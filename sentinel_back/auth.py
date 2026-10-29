import os
from functools import wraps

import jwt
from jwt import PyJWKClient
from flask import Blueprint, request, jsonify, g

auth_bp = Blueprint("auth", __name__, url_prefix="/auth")

SUPABASE_URL = os.environ["SUPABASE_URL"].rstrip("/")

# Busca (e guarda em cache) as chaves públicas do Supabase para validar a assinatura
_jwks = PyJWKClient(f"{SUPABASE_URL}/auth/v1/.well-known/jwks.json")


def token_required(f):
    """Exige um token de acesso válido do Supabase no header Authorization: Bearer <token>."""
    @wraps(f)
    def wrapper(*args, **kwargs):
        header = request.headers.get("Authorization", "")
        if not header.startswith("Bearer "):
            return jsonify({"erro": "Token ausente"}), 401

        token = header[7:].strip()
        try:
            chave = _jwks.get_signing_key_from_jwt(token)
            dados = jwt.decode(
                token,
                chave.key,
                algorithms=["ES256", "RS256"],
                audience="authenticated",
                issuer=f"{SUPABASE_URL}/auth/v1",
            )
        except jwt.ExpiredSignatureError:
            return jsonify({"erro": "Token expirado"}), 401
        except jwt.PyJWTError:
            return jsonify({"erro": "Token inválido"}), 401

        g.user_id = dados["sub"]
        g.email = dados.get("email")
        g.nome = (dados.get("user_metadata") or {}).get("name")
        return f(*args, **kwargs)
    return wrapper


@auth_bp.route("/me", methods=["GET"])
@token_required
def me():
    return jsonify({"id": g.user_id, "name": g.nome, "email": g.email}), 200
