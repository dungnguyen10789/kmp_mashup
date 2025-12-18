package vn.dna.kmp_mashup.domain.repository.user

import vn.dna.kmp_mashup.domain.entity.user.UserEntity

/**
 * Domain Repository Interface for User Operations.
 *
 * In Clean Architecture, the Domain layer defines *what* data operations are needed (Interfaces),
 * but not *how* they are implemented.
 *
 * The implementation of this interface resides in the Data layer (e.g., `UserRepositoryImpl`).
 * This inversion of control allows the Domain layer to remain independent of Data sources
 * (API, Database) and libraries (Ktor, Room, Realm).
 */
interface UserRepository {
    /**
     * Retrieves the current user's profile.
     *
     * @return A [Result] containing the [UserEntity] on success, or an exception on failure.
     */
    suspend fun getMyProfile(): Result<UserEntity>
}
