package br.com.sentinel.app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout

object HttpClientProvider {

    private const val DEFAULT_CONNECT_MS = 20_000L
    private const val DEFAULT_REQUEST_MS = 30_000L

    val client: HttpClient by lazy {
        HttpClient(Android) {
            expectSuccess = false
            install(HttpTimeout) {
                connectTimeoutMillis = DEFAULT_CONNECT_MS
                requestTimeoutMillis = DEFAULT_REQUEST_MS
                socketTimeoutMillis = DEFAULT_REQUEST_MS
            }
        }
    }
}
