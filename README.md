# Sentinel

Aplicativo Android que identifica golpes em mensagens, e-mails, telas e ligações, com um back end que faz a segunda análise com IA.

| Pasta | Conteúdo |
| :-- | :-- |
| `sentinel-android/` | App Android (Kotlin, Jetpack Compose). Veja o README da pasta. |
| `Sentinel_Back/` | API Flask no Render (Amazon Bedrock, Supabase). Veja o README da pasta. |
| `.github/workflows/` | CI do app (`android.yml`) e do back end (`backend.yml`). |

O servidor não guarda o texto das mensagens analisadas.
