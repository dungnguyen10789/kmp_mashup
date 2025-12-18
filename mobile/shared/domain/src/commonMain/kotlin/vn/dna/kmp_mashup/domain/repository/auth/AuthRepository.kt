package vn.dna.kmp_mashup.domain.repository.auth

import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.model.auth.LoginCredentials

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<TokenEntity>
    suspend fun logout(): Result<Unit>
}
