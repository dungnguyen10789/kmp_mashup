package vn.dna.kmp_mashup.domain.usecase.auth

import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import vn.dna.kmp_mashup.domain.repository.auth.AuthRepository
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository
import vn.dna.kmp_mashup.domain.usecase.base.BaseUseCase

class LogoutUseCase(
    private val repository: AuthRepository,
    private val tokenRepository: TokenRepository,
    private val authNotifier: AuthNotifier,
) : BaseUseCase<Unit, Unit>() {
    /**
     * Executes the logout logic.
     * 1. Calls the API (best-effort, result is ignored).
     * 2. Clears local tokens regardless of API call success.
     * 3. Notifies the app of the state change.
     */
    override suspend fun execute(params: Unit): Result<Unit> {
        repository.logout()
        tokenRepository.clearTokens()
        authNotifier.setUnauthenticated()
        return Result.success(Unit)
    }
}
