# Sentinel back end

Flask API (Render) that analyses SMS, notifications, e-mails, images and PDFs for scams, using
Amazon Bedrock (with Anthropic as fallback), and stores only anonymous results in Supabase.

## Routes

| Route | Method | Description |
|---|---|---|
| `/health` | GET | Liveness check |
| `/auth/perfil` | GET | Authenticated user profile |
| `/api/analisar` | POST | Analyse text and/or one file (`arquivo`: PNG, JPG, GIF, WEBP, PDF) |
| `/api/alertas` | GET | Regional scam alerts |
| `/api/denuncias` | POST | Report a scam (`descricao`, `regiao`) |

All routes except `/health` require `Authorization: Bearer <Supabase JWT>`.

## Privacy

The analysed text is never stored. `analises` keeps only origin app, verdict, confidence and
reasons. Reports sent through `/api/denuncias` are stored as written by the user.

## Configuration

Copy `.env.example` and fill it. The app refuses to start on an invalid `PROVEDOR_IA`, a malformed
model id, a missing `SUPABASE_URL`, or `PROVEDOR_IA=anthropic` without `ANTHROPIC_API_KEY`.

If the primary provider fails, the other one is tried (Anthropic only handles text). If both fail,
simple local rules are used for text.

## Database

Run `schema.sql` in the Supabase SQL editor. RLS is enabled on every table; the service key
bypasses it.

## Development

```
pip install -r requirements-dev.txt
ruff check . && ruff format --check .
mypy .
pytest
```

Run with `gunicorn main:app`, or build the `Dockerfile`.
