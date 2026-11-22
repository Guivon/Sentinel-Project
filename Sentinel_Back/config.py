import os
import re

VALID_PROVIDERS = ("bedrock", "anthropic")
MODEL_ID_PATTERN = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:\-]{2,127}$")


def supabase_url() -> str:
    url = os.environ.get("SUPABASE_URL", "").strip().rstrip("/")
    if not url:
        raise RuntimeError("A variável de ambiente SUPABASE_URL não está configurada.")
    return url


def validate_environment() -> None:
    supabase_url()

    provider = os.environ.get("PROVEDOR_IA", "bedrock").strip().lower()
    if provider not in VALID_PROVIDERS:
        raise RuntimeError(f"PROVEDOR_IA inválido: {provider!r}. Use um de {VALID_PROVIDERS}.")

    models = {
        "BEDROCK_MODEL_ID": os.environ.get("BEDROCK_MODEL_ID", "us.amazon.nova-pro-v1:0"),
        "ANTHROPIC_MODEL": os.environ.get("ANTHROPIC_MODEL", "claude-haiku-5-5"),
    }
    for name, value in models.items():
        if not MODEL_ID_PATTERN.match(value.strip()):
            raise RuntimeError(f"{name} inválido: {value!r}.")

    if provider == "anthropic" and not os.environ.get("ANTHROPIC_API_KEY"):
        raise RuntimeError("PROVEDOR_IA=anthropic exige ANTHROPIC_API_KEY.")
