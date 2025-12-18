package vn.dna.kmp_mashup.data.network.gateway

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.gateway.auth.RefreshTokenGateway
import vn.dna.kmp_mashup.dto.auth.TokenDTO
import vn.dna.kmp_mashup.data.repository.BaseRepositoryImpl

/**
 * Implementation of [RefreshTokenGateway].
 *
 * This class is responsible for making the actual network call to the `/auth/refresh-token` endpoint.
 *
 * @property publicClient A special HttpClient (configured via DI) that does NOT have the AuthInterceptor attached.
 *                        Reason: If we used the regular client, the interceptor would intercept the refresh request itself,
 *                        try to refresh the token again -> causing an infinite loop.
 */
class RefreshTokenGatewayImpl(
    private val publicClient: HttpClient
) : RefreshTokenGateway, BaseRepositoryImpl() {

    override suspend fun refresh(refreshToken: String): Result<TokenEntity> {
        // Use safeApiCall from BaseRepositoryImpl to handle network errors (try-catch, mapping)
        return safeApiCall<TokenDTO> {
            publicClient.post("auth/refresh-token") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to refreshToken))
            }
        }.map { dto ->
            // Map from DTO (Data Transfer Object) to Entity (Domain Object)
            TokenEntity(accessToken = dto.accessToken, refreshToken = dto.refreshToken)
        }
    }
}
