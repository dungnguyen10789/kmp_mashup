package vn.dna.kmp_mashup.domain.repository.auth

import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.model.auth.LoginRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<TokenEntity>
    suspend fun logout(): Result<Unit>
}
