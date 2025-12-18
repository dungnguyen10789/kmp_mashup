package vn.dna.kmp_mashup.domain.repository.user

import kotlinx.coroutines.flow.Flow
import vn.dna.kmp_mashup.domain.entity.user.UserEntity

/**
 * Domain Repository Interface for User Operations.
 */
interface UserRepository {
    /**
     * Fetches the current user's profile from the network and caches it.
     */
    suspend fun getMyProfile(): Result<UserEntity>

    /**
     * Fetches a specific user's profile by ID from the network.
     */
    suspend fun getUserProfile(id: String): Result<UserEntity>

    /**
     * Observes the current user's profile from the local source of truth.
     */
    fun observeMyProfile(): Flow<UserEntity?>
}
