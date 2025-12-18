package vn.dna.kmp_mashup.domain.entity.auth

/**
 * Domain Entity: Token
 *
 * Represents the authentication tokens within the business logic layer.
 *
 * Entities in the Domain layer are pure Kotlin classes free from any framework annotations
 * (like @Serializable from Serialization or @Entity from Room).
 * This separation ensures that changes in the Data layer (e.g., changing API JSON format)
 * do not directly affect the core business rules.
 */
data class TokenEntity(
    val accessToken: String,
    val refreshToken: String
)
