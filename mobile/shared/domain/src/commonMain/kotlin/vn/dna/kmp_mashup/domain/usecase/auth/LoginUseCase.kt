package vn.dna.kmp_mashup.domain.usecase.auth

import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.model.auth.LoginRequest
import vn.dna.kmp_mashup.domain.repository.auth.AuthRepository
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository
import vn.dna.kmp_mashup.domain.usecase.base.BaseUseCase

class LoginUseCase(
    private val repository: AuthRepository,
    private val tokenRepository: TokenRepository,
    private val authNotifier: AuthNotifier,
) : BaseUseCase<LoginRequest, TokenEntity>() {
    /**
     * Executes the login logic.
     * 1. Calls the API.
     * 2. Saves tokens on success.
     * 3. Notifies the app of the state change.
     */
    override suspend fun execute(params: LoginRequest): Result<TokenEntity> {
        return repository.login(params)
            .onSuccess { tokens ->
                // CRITICAL: Save tokens to the repository and notify the app state.
                tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken)
                authNotifier.setAuthenticated(userId = params.username)
            }
            .onFailure { throwable ->
                // Optional: Handle side effects of failure, like ensuring clean state.
                tokenRepository.clearTokens()
                authNotifier.setUnauthenticated()
            }
    }
}
