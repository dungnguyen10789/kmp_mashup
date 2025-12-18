package vn.dna.kmp_mashup.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import vn.dna.kmp_mashup.data.cache.CurrentUserHolder
import vn.dna.kmp_mashup.domain.entity.user.UserEntity
import vn.dna.kmp_mashup.domain.repository.user.UserRepository
import vn.dna.kmp_mashup.dto.user.UserDTO

/**
 * Repository implementation for User related operations.
 *
 * This class fetches raw DTOs from the API and maps them to clean Domain Entities.
 * It extends [BaseRepositoryImpl] to leverage centralized error handling (safeApiCall).
 */
class UserRepositoryImpl(
    private val httpClient: HttpClient,
    private val currentUserHolder: CurrentUserHolder
) : UserRepository, BaseRepositoryImpl() {

    /**
     * Fetches the current user's profile.
     *
     * @return Result<UserEntity> which encapsulates Success or Failure (Domain Exception).
     */
    override suspend fun getMyProfile(): Result<UserEntity> {
        // Execute the API call and catch any network errors
        return safeApiCall<UserDTO> {
            httpClient.get("users/me")
        }.map { dto ->
            // Map DTO -> Domain Entity
            // This decoupling ensures that API changes (e.g., field renaming)
            // only require changes in this mapping logic, not the whole app.
            UserEntity(
                id = dto.id,
                email = dto.email,
                username = dto.username,
                fullName = dto.fullname,
                gender = dto.gender
            )
        }
    }

    override suspend fun getUserProfile(id: String): Result<UserEntity> {
        return safeApiCall<UserDTO> {
            httpClient.get("users/$id")
        }.map { dto ->
            // Map DTO -> Domain Entity
            // This decoupling ensures that API changes (e.g., field renaming)
            // only require changes in this mapping logic, not the whole app.
            UserEntity(
                id = dto.id,
                username = dto.username,
                fullName = dto.fullname,
                email = dto.email,
                gender = dto.gender
            )
        }
    }

    override fun observeMyProfile(): Flow<UserEntity?> {
        return currentUserHolder.userFlow
    }
}
