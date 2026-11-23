import re
import unicodedata
from typing import Any

import cnpj as cnpj_check

PIX_PATTERN = re.compile(r"000201.*?6304[0-9A-Fa-f]{4}", re.DOTALL)
UUID_PATTERN = re.compile(r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
COMPANY_SUFFIXES = {"LTDA", "ME", "EPP", "SA", "S", "A", "EIRELI", "MEI", "CIA", "DE", "DA", "DO"}
MIN_NAME_MATCH = 0.5


def find_in_text(text: str) -> str | None:
    match = PIX_PATTERN.search(text or "")
    if not match:
        return None
    return re.sub(r"[\r\n\t]+", "", match.group(0))


def crc16(data: str) -> str:
    crc = 0xFFFF
    for byte in data.encode("utf-8"):
        crc ^= byte << 8
        for _ in range(8):
            crc = ((crc << 1) ^ 0x1021) & 0xFFFF if crc & 0x8000 else (crc << 1) & 0xFFFF
    return f"{crc:04X}"


def _tlv(payload: str) -> dict[str, str] | None:
    fields: dict[str, str] = {}
    position = 0
    while position < len(payload):
        if position + 4 > len(payload):
            return None
        tag, size = payload[position : position + 2], payload[position + 2 : position + 4]
        if not size.isdigit():
            return None
        end = position + 4 + int(size)
        if end > len(payload):
            return None
        fields[tag] = payload[position + 4 : end]
        position = end
    return fields


def classify_key(key: str) -> str:
    digits = cnpj_check.only_digits(key)
    if "@" in key:
        return "email"
    if key.startswith("+"):
        return "telefone"
    if UUID_PATTERN.match(key.lower()):
        return "aleatoria"
    if len(digits) == 14 and cnpj_check.is_valid(digits):
        return "cnpj"
    if len(digits) == 11:
        return "cpf"
    return "outra"


def parse(payload: str) -> dict[str, Any] | None:
    cleaned = re.sub(r"[\r\n\t]+", "", payload or "").strip()
    fields = _tlv(cleaned)
    if not fields or "63" not in fields:
        return None
    crc_ok = crc16(cleaned[:-4]) == cleaned[-4:].upper()
    key = ""
    dynamic = False
    for tag in range(26, 52):
        account = _tlv(fields.get(f"{tag:02d}", "")) if f"{tag:02d}" in fields else None
        if account and "br.gov.bcb.pix" in account.get("00", "").lower():
            key = account.get("01", "")
            dynamic = bool(account.get("25"))
            break
    return {
        "crc_ok": crc_ok,
        "key": key,
        "key_type": classify_key(key) if key else "dinamico" if dynamic else "desconhecida",
        "name": fields.get("59", ""),
    }


def key_cnpj(info: dict[str, Any] | None) -> str | None:
    if info and info["key_type"] == "cnpj":
        return cnpj_check.only_digits(info["key"])
    return None


def _tokens(name: str) -> set[str]:
    plain = unicodedata.normalize("NFD", name).encode("ascii", "ignore").decode().upper()
    return {token for token in re.findall(r"[A-Z0-9]+", plain) if token not in COMPANY_SUFFIXES}


def names_match(merchant: str, registered: list[str]) -> bool:
    wanted = _tokens(merchant)
    if not wanted:
        return True
    pool = set().union(*(_tokens(name) for name in registered)) if registered else set()
    hits = sum(1 for token in wanted if any(known.startswith(token) for known in pool))
    return hits / len(wanted) >= MIN_NAME_MATCH


def evaluate(
    info: dict[str, Any] | None, doc_cnpjs: list[str], checks: list[dict[str, Any]]
) -> dict[str, Any]:
    result: dict[str, Any] = {"alertas": [], "risco": False}
    if info is None:
        result["alertas"].append("Código Pix ilegível ou adulterado")
        result["risco"] = True
        return result
    result["tipo_chave"] = info["key_type"]
    if not info["crc_ok"]:
        result["alertas"].append("Código Pix adulterado: a verificação de integridade falhou")
        result["risco"] = True
    if not doc_cnpjs or info["key_type"] in {"dinamico", "desconhecida"}:
        return result
    registered = [
        name for item in checks if item["cnpj_id"] in doc_cnpjs for name in item.get("nomes", [])
    ]
    key_id = key_cnpj(info)
    if key_id and key_id not in doc_cnpjs:
        result["alertas"].append("O Pix vai para um CNPJ diferente do que está no documento")
        result["risco"] = True
    elif not key_id:
        result["alertas"].append("O Pix não usa o CNPJ do documento como chave")
        if registered and not names_match(info["name"], registered):
            result["alertas"].append("O nome do recebedor do Pix não bate com o da empresa")
            result["risco"] = True
    return result
