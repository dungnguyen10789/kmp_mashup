package vn.dna.kmp_mashup.domain.gateway.auth

import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity

/**
 * Gateway Interface for token refreshing operations.
 *
 * **Purpose:**
 * This interface decouples `TokenRepository` from the direct API call implementation for refreshing tokens.
 * This is crucial to break the Circular Dependency cycle:
 * `TokenRepository` -> `HttpClient` (to make API calls) -> `AuthInterceptor` -> `TokenRepository` (to get tokens).
 *
 * By using this Gateway, `TokenRepository` only knows "I need to refresh",
 * while the implementation of this Gateway handles *how* to refresh (specifically using a public HttpClient without interceptors).
 */
interface RefreshTokenGateway {
    /**
     * Sends a request to the server to refresh the authentication tokens.
     * @param refreshToken The current refresh token string.
     * @return A [Result] containing the new [TokenEntity] (accessToken + refreshToken) or a failure.
     */
    suspend fun refresh(refreshToken: String): Result<TokenEntity>
}
