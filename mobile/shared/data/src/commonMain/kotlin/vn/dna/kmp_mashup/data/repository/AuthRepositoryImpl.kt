package vn.dna.kmp_mashup.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import vn.dna.kmp_mashup.data.datasource.user.UserLocalDataSource
import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.model.auth.LoginRequest
import vn.dna.kmp_mashup.domain.repository.auth.AuthRepository
import vn.dna.kmp_mashup.dto.auth.TokenDTO

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val userLocalDataSource: UserLocalDataSource
) : BaseRepositoryImpl(), AuthRepository {

    override suspend fun login(request: LoginRequest): Result<TokenEntity> {
        return safeApiCall<TokenDTO> {
            httpClient.post("auth/login") {
                setBody(request)
            }
        }.map {
            TokenEntity(accessToken = it.accessToken, refreshToken = it.refreshToken)
        }
    }

    override suspend fun logout(): Result<Unit> {
        // IMPORTANT: Clear local user data from DB. This will trigger the reactive flow.
        userLocalDataSource.clear()

        // Then, attempt to notify the server. The local session is already terminated.
        return safeApiCall {
            httpClient.post("auth/logout")
        }
    }
}
