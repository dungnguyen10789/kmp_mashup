package vn.dna.kmp_mashup.domain.entity.user

data class UserEntity(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: Int
)