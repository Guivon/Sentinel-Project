import pytest
import requests

import analysis
import learning


class FakeResponse:
    def __init__(self, payload):
        self._payload = payload

    def raise_for_status(self):
        return None

    def json(self):
        return self._payload


@pytest.fixture(autouse=True)
def reset_cache(monkeypatch):
    monkeypatch.setitem(learning._cache, "at", None)
    monkeypatch.setitem(learning._cache, "block", "")
    monkeypatch.setenv("SUPABASE_SERVICE_KEY", "chave-de-teste")


def test_mask_hides_personal_data_and_tags():
    text = "Falou com joao@exemplo.com, ligou 11 91234-5678 e mandou https://golpe.com/x <b>oi</b>"
    masked = learning.mask(text)
    assert "joao@exemplo.com" not in masked
    assert "91234" not in masked
    assert "golpe.com" not in masked
    assert "<" not in masked and ">" not in masked
    assert "[e-mail]" in masked and "[link]" in masked and "[número]" in masked


def test_build_block_keeps_one_report_per_user_and_removes_duplicates():
    rows = [
        {"user_id": "a", "descricao": "Recebi um SMS pedindo recarga de celular"},
        {"user_id": "a", "descricao": "Outro relato do mesmo usuário sobre boleto"},
        {"user_id": "b", "descricao": "Recebi um SMS pedindo recarga de celular"},
        {"user_id": "c", "descricao": "Ligaram dizendo ser da central do banco"},
        {"user_id": "d", "descricao": "curto"},
        {"user_id": "e", "descricao": 123},
        "invalido",
    ]
    block = learning.build_block(rows)
    assert block.count("\n- ") == 2
    assert "recarga" in block and "central do banco" in block
    assert "boleto" not in block


def test_build_block_empty_returns_empty_text():
    assert learning.build_block([]) == ""


def test_build_block_respects_the_limit(monkeypatch):
    monkeypatch.setattr(learning, "EXAMPLES_MAX", 2)
    rows = [
        {"user_id": str(i), "descricao": f"Relato número diferente sobre golpe {i}x"}
        for i in range(5)
    ]
    assert learning.build_block(rows).count("\n- ") == 2


def test_examples_without_service_key_return_empty(monkeypatch):
    monkeypatch.delenv("SUPABASE_SERVICE_KEY", raising=False)
    assert learning.examples_block() == ""


def test_examples_are_cached(monkeypatch):
    calls = []

    def fake_get(*args, **kwargs):
        calls.append(1)
        return FakeResponse([{"user_id": "a", "descricao": "Pediram Pix para liberar prêmio"}])

    monkeypatch.setattr(learning.requests, "get", fake_get)
    first = learning.examples_block()
    second = learning.examples_block()
    assert first == second and "Pix" in first
    assert len(calls) == 1


def test_examples_fall_back_when_supabase_fails(monkeypatch):
    def failing_get(*args, **kwargs):
        raise requests.ConnectionError("fora do ar")

    monkeypatch.setattr(learning.requests, "get", failing_get)
    assert learning.examples_block() == ""


def test_system_prompt_includes_examples(monkeypatch):
    monkeypatch.setattr(analysis, "examples_block", lambda: "BLOCO DE EXEMPLOS")
    assert analysis._system_prompt().endswith("BLOCO DE EXEMPLOS")
    monkeypatch.setattr(analysis, "examples_block", lambda: "")
    assert analysis._system_prompt() == analysis.SYSTEM_PROMPT
