package vn.dna.kmp_mashup.domain.usecase.user

import vn.dna.kmp_mashup.domain.entity.user.UserEntity
import vn.dna.kmp_mashup.domain.model.error.Failure
import vn.dna.kmp_mashup.domain.model.error.FailureException
import vn.dna.kmp_mashup.domain.repository.user.UserRepository
import vn.dna.kmp_mashup.domain.usecase.base.BaseUseCase

class GetUserProfileUseCase(
    private val repository: UserRepository
) : BaseUseCase<Unit, UserEntity>() {
    // Use cases should always return Result<Entity>
    override suspend fun execute(params: Unit): Result<UserEntity> {
        println("GetUserProfileUseCase")

        // 1) Call repository (returns Result<UserEntity>)
        val result = repository.getMyProfile()

        println("GetUserProfileUseCase1 $result")

        // 2) Transform result with mapCatching
        return result.mapCatching { user ->
            // When result is Success, this block runs.

            // 3) Place business rules here if needed.
            // Example: if (user.age < 18) throw Exception("User is too young")

            user
        }.onFailure { throwable ->
            // When result is Failure, this block runs.

            // 4) Unwrap FailureException to inspect failure type.
            if (throwable is FailureException) {
                when (throwable.failure) {
                    is Failure.ApiError -> {
                        // Business rule example: if 401, clear session and rethrow
                        if (throwable.failure.code == 401) {
                            // repository.clearTokens() // example
                            // throw Exception("Session Expired")
                        }
                    }
                    else -> Unit // ignore other failures
                }
            }
            // 5) If we don't throw, the error is preserved and returned to presentation layer.
        }
    }

    suspend operator fun invoke() = invoke(Unit)
}
