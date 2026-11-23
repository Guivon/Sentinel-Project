import logging
import re
import time
from datetime import date
from typing import Any

import requests

logger = logging.getLogger("sentinel.cnpj")

LOOKUP_URL = "https://brasilapi.com.br/api/cnpj/v1/{cnpj}"
CNPJ_PATTERN = re.compile(r"(?<!\d)(\d{2}\.?\d{3}\.?\d{3}/?\d{4}-?\d{2})(?!\d)")
CACHE_TTL_S = 3600
MAX_CNPJS = 3
TIMEOUT_S = 4
YOUNG_MONTHS = 3
BAD_STATUS = {"BAIXADA", "INAPTA", "SUSPENSA", "NULA"}

_cache: dict[str, tuple[float, dict[str, Any]]] = {}


def only_digits(value: str) -> str:
    return re.sub(r"\D", "", value)


def is_valid(cnpj: str) -> bool:
    digits = only_digits(cnpj)
    if len(digits) != 14 or len(set(digits)) == 1:
        return False
    for size in (12, 13):
        weights = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
        if size == 13:
            weights = [6, *weights]
        total = sum(int(digits[i]) * weights[i] for i in range(size))
        check = 11 - total % 11
        check = 0 if check >= 10 else check
        if int(digits[size]) != check:
            return False
    return True


def extract(text: str, extra: Any = None) -> list[str]:
    found: list[str] = []
    candidates = CNPJ_PATTERN.findall(text or "")
    if isinstance(extra, list):
        candidates += [item for item in extra if isinstance(item, str)]
    for candidate in candidates:
        digits = only_digits(candidate)
        if len(digits) == 14 and digits not in found:
            found.append(digits)
    return found[:MAX_CNPJS]


def mask(cnpj: str) -> str:
    return f"{cnpj[:2]}.***.***/{cnpj[8:12]}-{cnpj[12:]}"


def _lookup(cnpj: str) -> tuple[str, dict[str, Any] | None]:
    cached = _cache.get(cnpj)
    if cached and time.time() - cached[0] < CACHE_TTL_S:
        return "ok", cached[1]
    try:
        response = requests.get(LOOKUP_URL.format(cnpj=cnpj), timeout=TIMEOUT_S)
    except requests.RequestException as error:
        logger.warning("CNPJ lookup failed: %r", error)
        return "indisponivel", None
    if response.status_code in (400, 404):
        return "inexistente", None
    if response.status_code != 200:
        return "indisponivel", None
    try:
        data = response.json()
    except ValueError:
        return "indisponivel", None
    _cache[cnpj] = (time.time(), data)
    return "ok", data


def _months_since(opening: str | None) -> int | None:
    if not opening:
        return None
    try:
        start = date.fromisoformat(opening[:10])
    except ValueError:
        return None
    today = date.today()
    return (today.year - start.year) * 12 + today.month - start.month


def verify(cnpj: str) -> dict[str, Any]:
    result: dict[str, Any] = {
        "cnpj": mask(cnpj),
        "cnpj_id": cnpj,
        "situacao": "desconhecida",
        "alertas": [],
        "nomes": [],
    }
    if not is_valid(cnpj):
        result["situacao"] = "invalido"
        result["alertas"].append("CNPJ inválido: os dígitos verificadores não conferem")
        return result
    status, data = _lookup(cnpj)
    if status == "inexistente":
        result["situacao"] = "inexistente"
        result["alertas"].append("CNPJ não encontrado na Receita Federal")
        return result
    if status != "ok" or data is None:
        result["alertas"].append("Não foi possível consultar o CNPJ agora")
        return result
    result["nomes"] = [
        str(data[field]) for field in ("razao_social", "nome_fantasia") if data.get(field)
    ]
    registration = str(data.get("descricao_situacao_cadastral") or "").upper()
    result["situacao"] = registration.lower() or "ativa"
    if registration in BAD_STATUS:
        result["alertas"].append(f"CNPJ com situação cadastral {registration.lower()}")
    months = _months_since(data.get("data_inicio_atividade"))
    if months is not None and months < YOUNG_MONTHS:
        result["recente"] = True
        result["alertas"].append("Empresa aberta há menos de 3 meses")
    return result


def verify_all(text: str, extra: Any = None) -> list[dict[str, Any]]:
    return [verify(cnpj) for cnpj in extract(text, extra)]


def is_risky(results: list[dict[str, Any]]) -> bool:
    risky = {"invalido", "inexistente", *(status.lower() for status in BAD_STATUS)}
    return any(item["situacao"] in risky or item.get("recente") for item in results)
