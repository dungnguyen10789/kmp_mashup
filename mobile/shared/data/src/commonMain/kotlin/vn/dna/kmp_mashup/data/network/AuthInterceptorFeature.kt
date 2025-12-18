package vn.dna.kmp_mashup.data.network

import io.ktor.client.*
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.*
import io.ktor.http.*
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository

/**
 * Custom Ktor Plugin for handling Authentication.
 *
 * **Why not use the built-in `Auth` plugin?**
 * 1. The built-in `Auth` plugin can be complex and sometimes hard to customize for specific refresh token behaviors.
 * 2. More importantly, writing a custom interceptor gives us full control over *when* `ensureValidAccessToken` is called.
 *    We can prevent the request from being sent at all if no valid token is available, rather than sending it -> getting 401 -> and then refreshing.
 *
 * **Mechanism:**
 * This plugin intercepts the request pipeline at the [HttpSendPipeline.State] phase.
 * It checks if the request requires a token (based on URL). If so, it calls [TokenRepository.ensureValidAccessToken]
 * to retrieve a token (which may involve refreshing if the current one is expired).
 */
val AuthInterceptor = createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
    val tokenRepository = pluginConfig.tokenRepository

    // Intercept at the State phase (Before the request is transformed and sent)
    client.sendPipeline.intercept(HttpSendPipeline.State) { context ->
        val request = context as HttpRequestBuilder

        val path = request.url.encodedPath
        // List of public APIs that do not require a token attached.
        // TODO: Consider moving this list to a config or using annotations for scalability.
        val publicPaths = listOf("/auth/login", "/auth/register", "/auth/refresh-token")
        
        // If it's a public API -> Skip, let the request proceed.
        if (publicPaths.any { path.contains(it) }) return@intercept

        // If it's a protected API:
        // Call the repository to ensure a valid access token exists.
        // This function will automatically refresh the token if necessary.
        val accessToken = tokenRepository.ensureValidAccessToken()
            ?: throw Exception("No valid access token after refresh") // Throw exception to stop the request immediately.

        // Attach the token to the Authorization Header.
        request.headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
    }
}

/**
 * Configuration class for [AuthInterceptor].
 * Allows injecting [TokenRepository] into the plugin.
 */
class AuthInterceptorConfig {
    lateinit var tokenRepository: TokenRepository
}

/**
 * Extension function to easily install the AuthInterceptor into HttpClient.
 */
fun HttpClientConfig<*>.installAuthInterceptor(tokenRepository: TokenRepository) {
    install(AuthInterceptor) {
        this.tokenRepository = tokenRepository
    }
}
