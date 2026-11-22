# Sentinel

Android app (Kotlin, Jetpack Compose) that warns about scam messages. SMS, WhatsApp notifications,
Gmail messages and on-screen text are checked on the device first; only content that already looks
suspicious (or text/images/PDFs the user sends manually) goes to the Sentinel back end for an AI check.

## Requirements

- Android Studio with JDK 17 or newer
- A Supabase project (auth) and the Sentinel back end running (see the `Sentinel_Back` repository)

## Setup

1. Copy `local.properties.example` to `local.properties` and fill in `SUPABASE_URL`, `SUPABASE_KEY`
   and, if you are not using the default server, `BACKEND_URL`.
2. Open the project in Android Studio and let Gradle sync.
3. Run the `app` configuration. Unit tests: `./gradlew :app:testDebugUnitTest`.

`applicationId` / package: `br.com.sentinel.app`.

### Gmail connection

Gmail access uses Google OAuth, which is tied to the package name and the signing certificate
SHA-1. In Google Cloud Console create an Android OAuth client for `br.com.sentinel.app` with the
SHA-1 of your debug and release keys, and enable the Gmail API.

## Structure

```
app/src/main/java/br/com/sentinel/app
  *.kt            detection, back end client, Gmail, notifications, state
  ui/             SentinelApp (navigation), AnalysisViewModel, shared UI helpers
  ui/screens/     one file per screen (large screens have a *Components/*Sections companion)
  ui/components/  reusable composables
  ui/theme/       colours (SentinelColors.kt), typography, theme
```

All user-visible text is in `app/src/main/res/values/strings.xml`. The JSON contract with the back
end keeps Portuguese field names (`suspeito`, `pontuacao`, `motivos`, ...); the code is in English.

## Privacy

The back end never stores analysed text. See the consent screen and `PrivacyPolicyScreen`.

## Style

`.editorconfig` configures ktlint and Android Studio (explicit imports, 120 columns). Use
*Code > Optimize Imports* to expand any remaining wildcard imports.
