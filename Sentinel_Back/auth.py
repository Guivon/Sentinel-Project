import logging
from collections.abc import Callable
from functools import wraps
from typing import Any

import jwt
from flask import Blueprint, Response, g, jsonify, request
from jwt import PyJWKClient

from config import supabase_url

logger = logging.getLogger("sentinel.auth")

auth_bp = Blueprint("autenticacao", __name__, url_prefix="/auth")

_jwks: PyJWKClient | None = None


def _jwks_client() -> PyJWKClient:

    global _jwks
    if _jwks is None:
        _jwks = PyJWKClient(f"{supabase_url()}/auth/v1/.well-known/jwks.json")
    return _jwks


def require_token(view: Callable[..., Any]) -> Callable[..., Any]:

    @wraps(view)
    def wrapper(*args: Any, **kwargs: Any) -> Any:
        header = request.headers.get("Authorization", "")
        if not header.startswith("Bearer "):
            return jsonify({"erro": "Token ausente"}), 401

        token = header[7:].strip()
        try:
            key = _jwks_client().get_signing_key_from_jwt(token)
            payload = jwt.decode(
                token,
                key.key,
                algorithms=["ES256", "RS256"],
                audience="authenticated",
                issuer=f"{supabase_url()}/auth/v1",
            )
        except jwt.ExpiredSignatureError:
            return jsonify({"erro": "Token expirado"}), 401
        except jwt.PyJWTError as error:
            logger.info("Token recusado: %s", error.__class__.__name__)
            return jsonify({"erro": "Token inválido"}), 401

        g.user_id = payload["sub"]
        g.email = payload.get("email")
        g.name = (payload.get("user_metadata") or {}).get("name")
        return view(*args, **kwargs)

    return wrapper


@auth_bp.route("/perfil", methods=["GET"])
@require_token
def profile() -> tuple[Response, int]:

    return jsonify({"id": g.user_id, "nome": g.name, "email": g.email}), 200
