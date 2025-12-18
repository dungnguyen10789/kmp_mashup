package vn.dna.kmp_mashup.domain.repository.auth

import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity

interface TokenRepository {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
    suspend fun ensureValidAccessToken(): String?
    suspend fun refreshAfterUnauthorized(): Result<TokenEntity>
}