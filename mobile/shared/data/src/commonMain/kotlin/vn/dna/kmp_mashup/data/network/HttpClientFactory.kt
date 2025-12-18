package vn.dna.kmp_mashup.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import vn.dna.kmp_mashup.domain.config.normalizeBaseUrlForPlatform
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository

/**
 * Factory for creating [HttpClient] instances.
 *
 * **Purpose:**
 * Centralizes the configuration of Ktor HttpClient. This helps in:
 * - Reusing common configuration (baseUrl, JSON serialization, logging).
 * - Easily switching between different types of HttpClients (public vs private).
 * - Explicitly managing dependencies for HttpClient.
 */

class HttpClientFactory(baseUrl: String) {
    private val normalizedUrl = normalizeBaseUrlForPlatform(baseUrl)

    /**
     * Creates a "Public" HttpClient that has NO authentication mechanism.
     *
     * This client is used for APIs that do not require a token, such as:
     * - `/auth/login`
     * - `/auth/register`
     * - `/auth/refresh-token`
     *
     * It is injected into [RefreshTokenGatewayImpl] to break the dependency cycle.
     *
     * @param engine Ktor Engine for the specific platform (Android/iOS).
     * @return A configured HttpClient instance.
     */

    fun createPublicClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true // Throws exception for non-2xx status codes.
            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom(normalizedUrl))
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true; isLenient = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KtorLog [Public]: $message")
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    /**
     * Creates a "Private" HttpClient protected by [AuthInterceptor].
     *
     * This is the main client of the application, used for all APIs requiring authentication.
     * It will automatically attach and refresh tokens.
     *
     * @param engine Ktor Engine for the specific platform (Android/iOS).
     * @param tokenRepository Dependency required by [AuthInterceptor] to retrieve and refresh tokens.
     * @return A configured HttpClient instance.
     */

    fun create(engine: HttpClientEngine, tokenRepository: TokenRepository): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom(normalizedUrl))
                contentType(ContentType.Application.Json)

                // runBlocking allows calling suspend in a non-suspend lambda
                runBlocking {
                    val path = url.encodedPath

                    if (path.contains("/auth")) {
                        if (path.contains("/logout")) {
                            val accessToken = tokenRepository.ensureValidAccessToken()
                            if (accessToken != null) {
                                headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
                            }
                            return@runBlocking
                        }

                        // Other auth endpoints (login, register, refresh-token, etc.) → DO NOT attach token
                        return@runBlocking
                    }

                    val accessToken = tokenRepository.ensureValidAccessToken()
                    if (accessToken != null) {
                        headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KtorLog [Private]: $message")
                    }
                }
            }
        }
    }
}