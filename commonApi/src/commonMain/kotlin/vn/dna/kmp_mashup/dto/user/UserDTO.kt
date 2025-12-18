package vn.dna.kmp_mashup.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) for User information.
 *
 * This class represents the contract for User data exchanged between the Backend and Client.
 *
 * @property id The unique identifier of the user (UUID string).
 * @property username The user's login username.
 * @property fullname The display name of the user.
 * @property gender The gender code (0=Unknown, 1=Male, 2=Female). Consider using an Enum in the future.
 * @property email The registered email address.
 */
@Serializable
data class UserDTO(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("fullname") val fullname: String,
    @SerialName("gender") val gender: Int,
    @SerialName("email") val email: String,
)
