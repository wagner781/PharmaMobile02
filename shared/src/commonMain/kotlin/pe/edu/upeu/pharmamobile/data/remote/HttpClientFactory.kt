package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro

const val URL_BASE = "http://10.0.2.2:8080"

fun crearHttpClient(
    motor: HttpClientEngine,
    almacen: AlmacenSeguro,
    esDepuracion: Boolean = true
): HttpClient =
    HttpClient(motor) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }

        defaultRequest {
            url(URL_BASE)
            contentType(ContentType.Application.Json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    almacen.leerToken()?.let { BearerTokens(it, "") }
                }
                sendWithoutRequest { peticion ->
                    !peticion.url.encodedPath.endsWith("/auth/login")
                }
            }
        }

        install(Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    println("KtorLog: $message")
                }
            }
            level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
            sanitizeHeader { nombre -> nombre == HttpHeaders.Authorization }
        }
    }
