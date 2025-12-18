package vn.dna.kmp_mashup.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import vn.dna.kmp_mashup.dto.auth.TokenDTO
import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.model.auth.LoginCredentials
import vn.dna.kmp_mashup.domain.repository.auth.AuthRepository

class AuthRepositoryImpl(
    private val httpClient: HttpClient
) : BaseRepositoryImpl(), AuthRepository {

    override suspend fun login(credentials: LoginCredentials): Result<TokenEntity> {
        return safeApiCall<TokenDTO> {
            httpClient.post("auth/login") {
                setBody(mapOf(
                    "username" to credentials.username,
                    "password" to credentials.password
                ))
            }
        }.map {
            TokenEntity(accessToken = it.accessToken, refreshToken = it.refreshToken)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCall<Unit> {
            httpClient.post("auth/logout")
        }
    }
}
