import os
import sys
import time
import types
from pathlib import Path

import jwt
import pytest
from cryptography.hazmat.primitives.asymmetric import rsa

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
os.environ.setdefault("SUPABASE_URL", "https://teste.supabase.co")

PRIVATE_KEY = rsa.generate_private_key(public_exponent=65537, key_size=2048)


class FakeJwks:
    def get_signing_key_from_jwt(self, _token):
        return types.SimpleNamespace(key=PRIVATE_KEY.public_key())


def make_token(sub="usuario-teste", expires_at=3600):
    now = int(time.time())
    return jwt.encode(
        {
            "sub": sub,
            "aud": "authenticated",
            "iss": "https://teste.supabase.co/auth/v1",
            "iat": now,
            "exp": now + expires_at,
            "email": "teste@exemplo.com",
        },
        PRIVATE_KEY,
        algorithm="RS256",
    )


@pytest.fixture()
def header():
    return {"Authorization": f"Bearer {make_token()}"}


@pytest.fixture()
def api_client(monkeypatch):

    import analysis
    import auth
    from limits import WindowLimiter
    from main import create_app

    monkeypatch.setattr(auth, "_jwks_client", lambda: FakeJwks())
    monkeypatch.setattr(analysis, "AI_PROVIDER", "nenhum")
    monkeypatch.setattr(analysis, "SUPABASE_SERVICE_KEY", "")
    monkeypatch.setattr(analysis, "limiter", WindowLimiter(maximum=30, window_s=60))
    app = create_app()
    app.config["TESTING"] = True
    return app.test_client()
