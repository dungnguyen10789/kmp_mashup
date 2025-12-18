package vn.dna.kmp_mashup.domain.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
    // In production, prefer a secure wrapper type over raw String if possible.
)