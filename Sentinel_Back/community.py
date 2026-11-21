import logging
import os
from typing import Any

import requests
from flask import Blueprint, Response, g, jsonify, request

from auth import require_token
from config import supabase_url
from limits import WindowLimiter

logger = logging.getLogger("sentinel.community")

community_bp = Blueprint("community", __name__, url_prefix="/api")

ALERTS_SHOWN = 5
DESCRIPTION_MIN = 10
DESCRIPTION_MAX = 1000
REGION_MAX = 60

report_limiter = WindowLimiter(
    maximum=int(os.environ.get("LIMITE_DENUNCIAS", "5")),
    window_s=float(os.environ.get("LIMITE_DENUNCIAS_JANELA_S", "3600")),
)


def _headers(prefer: str | None = None) -> dict[str, str]:
    key = os.environ.get("SUPABASE_SERVICE_KEY", "")
    headers = {"apikey": key, "Content-Type": "application/json"}
    if not key.startswith("sb_"):
        headers["Authorization"] = f"Bearer {key}"
    if prefer:
        headers["Prefer"] = prefer
    return headers


def _error(message: str, status: int) -> tuple[Response, int]:
    return jsonify({"erro": message}), status


def _clean_text(value: Any, maximum: int) -> str | None:
    if value is None:
        return ""
    if not isinstance(value, str):
        return None
    return " ".join(value.split())[:maximum]


@community_bp.route("/alertas", methods=["GET"])
@require_token
def regional_alerts() -> tuple[Response, int]:
    if not os.environ.get("SUPABASE_SERVICE_KEY"):
        return jsonify({"alertas": []}), 200
    try:
        returned = requests.get(
            f"{supabase_url()}/rest/v1/alertas_regionais",
            headers=_headers(),
            params={
                "select": "titulo,regiao,criado_em",
                "order": "criado_em.desc",
                "limit": str(ALERTS_SHOWN),
            },
            timeout=10,
        )
        returned.raise_for_status()
        rows = returned.json()
    except (requests.RequestException, ValueError) as error:
        logger.warning("Falha ao buscar alertas regionais: %r", error)
        return _error("Não foi possível carregar os alertas agora.", 503)
    return jsonify({"alertas": rows}), 200


@community_bp.route("/denuncias", methods=["POST"])
@require_token
def report_scam() -> tuple[Response, int]:
    allowed, wait = report_limiter.try_acquire(g.user_id)
    if not allowed:
        response, status = _error("Você enviou muitos relatos. Tente novamente mais tarde.", 429)
        response.headers["Retry-After"] = str(wait)
        return response, status

    fields = request.get_json(silent=True)
    if not isinstance(fields, dict):
        return _error("Envie um JSON com o campo 'descricao'", 400)

    description = _clean_text(fields.get("descricao"), DESCRIPTION_MAX)
    region = _clean_text(fields.get("regiao"), REGION_MAX)
    if description is None or region is None:
        return _error("Os campos 'descricao' e 'regiao' devem ser texto", 400)
    if len(description) < DESCRIPTION_MIN:
        return _error(f"Descreva o golpe com pelo menos {DESCRIPTION_MIN} caracteres", 400)

    if not os.environ.get("SUPABASE_SERVICE_KEY"):
        return _error("Não foi possível registrar agora. Tente novamente.", 503)
    try:
        returned = requests.post(
            f"{supabase_url()}/rest/v1/denuncias",
            headers=_headers("return=minimal"),
            json={"user_id": g.user_id, "descricao": description, "regiao": region or None},
            timeout=10,
        )
        returned.raise_for_status()
    except requests.RequestException as error:
        logger.warning("Falha ao salvar denúncia: %r", error)
        return _error("Não foi possível registrar agora. Tente novamente.", 503)
    return jsonify({"ok": True}), 201
