import pytest

import community
from limits import WindowLimiter


class FakeResponse:
    def __init__(self, payload=None):
        self._payload = payload if payload is not None else []

    def raise_for_status(self):
        return None

    def json(self):
        return self._payload


@pytest.fixture()
def community_client(api_client, monkeypatch):
    monkeypatch.setenv("SUPABASE_SERVICE_KEY", "chave-de-teste")
    monkeypatch.setattr(community, "report_limiter", WindowLimiter(maximum=2, window_s=60))
    return api_client


def test_alerts_require_token(community_client):
    assert community_client.get("/api/alertas").status_code == 401


def test_alerts_without_service_key_return_empty_list(api_client, header, monkeypatch):
    monkeypatch.delenv("SUPABASE_SERVICE_KEY", raising=False)
    returned = api_client.get("/api/alertas", headers=header)
    assert returned.status_code == 200
    assert returned.get_json() == {"alertas": []}


def test_alerts_return_rows_from_supabase(community_client, header, monkeypatch):
    rows = [
        {
            "titulo": "Falso Pix",
            "regiao": "São Paulo - SP",
            "criado_em": "2026-11-21T10:00:00+00:00",
        }
    ]
    monkeypatch.setattr(community.requests, "get", lambda *a, **k: FakeResponse(rows))
    returned = community_client.get("/api/alertas", headers=header)
    assert returned.status_code == 200
    assert returned.get_json() == {"alertas": rows}


def test_report_saves_and_returns_201(community_client, header, monkeypatch):
    sent = {}

    def fake_post(url, **kwargs):
        sent.update(kwargs["json"])
        return FakeResponse()

    monkeypatch.setattr(community.requests, "post", fake_post)
    returned = community_client.post(
        "/api/denuncias",
        headers=header,
        json={"descricao": "Ligaram dizendo ser do banco", "regiao": "Recife - PE"},
    )
    assert returned.status_code == 201
    assert sent["user_id"] == "usuario-teste"
    assert sent["regiao"] == "Recife - PE"


def test_report_with_short_description_returns_400(community_client, header):
    returned = community_client.post("/api/denuncias", headers=header, json={"descricao": "curto"})
    assert returned.status_code == 400


def test_report_with_wrong_type_returns_400(community_client, header):
    returned = community_client.post(
        "/api/denuncias", headers=header, json={"descricao": 123456789012}
    )
    assert returned.status_code == 400


def test_report_rate_limit(community_client, header, monkeypatch):
    monkeypatch.setattr(community.requests, "post", lambda *a, **k: FakeResponse())
    body = {"descricao": "Mensagem pedindo código de verificação"}
    assert community_client.post("/api/denuncias", headers=header, json=body).status_code == 201
    assert community_client.post("/api/denuncias", headers=header, json=body).status_code == 201
    assert community_client.post("/api/denuncias", headers=header, json=body).status_code == 429
