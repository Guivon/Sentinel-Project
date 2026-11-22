from conftest import make_token


def test_profile_with_valid_token(api_client):
    r = api_client.get("/auth/perfil", headers={"Authorization": f"Bearer {make_token('u1')}"})
    assert r.status_code == 200 and r.get_json()["id"] == "u1"


def test_expired_token_returns_401(api_client):
    r = api_client.get(
        "/auth/perfil", headers={"Authorization": f"Bearer {make_token(expires_at=-10)}"}
    )
    assert r.status_code == 401 and "expirado" in r.get_json()["erro"]
