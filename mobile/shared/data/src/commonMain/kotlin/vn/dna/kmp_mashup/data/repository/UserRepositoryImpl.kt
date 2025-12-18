package vn.dna.kmp_mashup.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import vn.dna.kmp_mashup.data.cache.CurrentUserHolder
import vn.dna.kmp_mashup.data.datasource.user.UserLocalDataSource
import vn.dna.kmp_mashup.domain.entity.user.UserEntity
import vn.dna.kmp_mashup.domain.repository.user.UserRepository
import vn.dna.kmp_mashup.dto.user.UserDTO

class UserRepositoryImpl(
    private val httpClient: HttpClient,
    private val localDataSource: UserLocalDataSource, // Dependency for local caching
    private val currentUserHolder: CurrentUserHolder
) : UserRepository, BaseRepositoryImpl() {

    override suspend fun getMyProfile(): Result<UserEntity> {
        return safeApiCall<UserDTO> {
            httpClient.get("users/me")
        }.map { dto ->
            UserEntity(
                id = dto.id,
                email = dto.email,
                username = dto.username,
                fullName = dto.fullname,
                gender = dto.gender
            )
        }.onSuccess { userEntity ->
            // After fetching from network, save to local DB.
            // This will automatically trigger the CurrentUserHolder to update.
            localDataSource.saveUser(userEntity)
        }
    }

    override suspend fun getUserProfile(id: String): Result<UserEntity> {
        return safeApiCall<UserDTO> {
            httpClient.get("users/$id")
        }.map { dto ->
            UserEntity(
                id = dto.id,
                email = dto.email,
                username = dto.username,
                fullName = dto.fullname,
                gender = dto.gender
            )
        }
    }

    override fun observeMyProfile(): Flow<UserEntity?> {
        return currentUserHolder.userFlow
    }
}
