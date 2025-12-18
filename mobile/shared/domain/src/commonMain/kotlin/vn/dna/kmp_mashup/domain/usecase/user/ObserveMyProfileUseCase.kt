package vn.dna.kmp_mashup.domain.usecase.user

import kotlinx.coroutines.flow.Flow
import vn.dna.kmp_mashup.domain.entity.user.UserEntity
import vn.dna.kmp_mashup.domain.repository.user.UserRepository

/**
 * Use case for observing the current user's profile from the repository.
 * This respects the Clean Architecture by depending only on the Domain layer interface.
 */
class ObserveMyProfileUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<UserEntity?> = userRepository.observeMyProfile()
}
