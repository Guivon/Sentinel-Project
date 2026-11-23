import analysis
import cnpj
import pix


def build(key, name="LOJA EXEMPLO", city="SAO PAULO"):
    account = f"0014br.gov.bcb.pix01{len(key):02d}{key}"
    body = (
        "000201"
        f"26{len(account):02d}{account}"
        "52040000"
        "5303986"
        f"59{len(name):02d}{name}"
        f"60{len(city):02d}{city}"
        "6304"
    )
    return body + pix.crc16(body)


def test_valid_payload_parses_with_good_crc():
    info = pix.parse(build("11222333000181"))
    assert info is not None
    assert info["crc_ok"]
    assert info["key_type"] == "cnpj"
    assert info["name"] == "LOJA EXEMPLO"


def test_tampered_payload_fails_crc():
    payload = build("11222333000181").replace("LOJA", "LOJB")
    info = pix.parse(payload)
    assert info is not None
    assert not info["crc_ok"]


def test_find_in_text_extracts_copy_and_paste_code():
    payload = build("11222333000181")
    assert pix.find_in_text(f"Pague aqui: {payload} obrigado") == payload


def test_key_types_are_classified():
    assert pix.classify_key("a@b.com") == "email"
    assert pix.classify_key("+5511999990000") == "telefone"
    assert pix.classify_key("123e4567-e89b-12d3-a456-426614174000") == "aleatoria"
    assert pix.classify_key("12345678901") == "cpf"


def test_pix_to_different_cnpj_is_risky():
    info = pix.parse(build("11444777000161"))
    result = pix.evaluate(info, ["11222333000181"], [])
    assert result["risco"]


def test_pix_to_personal_key_with_other_name_is_risky():
    info = pix.parse(build("a@b.com", name="JOAO DA SILVA"))
    checks = [{"cnpj_id": "11222333000181", "nomes": ["LOJA EXEMPLO COMERCIO LTDA"]}]
    result = pix.evaluate(info, ["11222333000181"], checks)
    assert result["risco"]


def test_pix_to_same_cnpj_is_clean():
    info = pix.parse(build("11222333000181"))
    checks = [{"cnpj_id": "11222333000181", "nomes": ["LOJA EXEMPLO COMERCIO LTDA"]}]
    result = pix.evaluate(info, ["11222333000181"], checks)
    assert not result["alertas"]


def test_names_match_handles_truncation():
    assert pix.names_match("LOJA EXEMPLO COM", ["LOJA EXEMPLO COMERCIO LTDA"])
    assert not pix.names_match("JOAO SILVA", ["LOJA EXEMPLO COMERCIO LTDA"])


def test_apply_pix_raises_result_to_high():
    normalized = {
        "suspeito": False,
        "pontuacao": 10,
        "nivel": "baixo",
        "confianca": 0.1,
        "motivos": [],
    }
    analysis._apply_pix(normalized, {"alertas": ["x"], "risco": True})
    assert normalized["nivel"] == "alto"
    assert normalized["pontuacao"] >= analysis.PIX_RISK_SCORE


def test_cnpj_verify_returns_registered_names(monkeypatch):
    data = {"razao_social": "LOJA EXEMPLO LTDA", "descricao_situacao_cadastral": "ATIVA"}
    monkeypatch.setattr(cnpj, "_lookup", lambda _c: ("ok", data))
    assert cnpj.verify("11222333000181")["nomes"] == ["LOJA EXEMPLO LTDA"]
