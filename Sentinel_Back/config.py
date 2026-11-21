import os


def supabase_url() -> str:

    url = os.environ.get("SUPABASE_URL", "").strip().rstrip("/")
    if not url:
        raise RuntimeError("A variável de ambiente SUPABASE_URL não está configurada.")
    return url


def validate_environment() -> None:

    supabase_url()
