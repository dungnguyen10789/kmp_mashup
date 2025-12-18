package vn.dna.kmp_mashup.domain.usecase.auth

import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository
import vn.dna.kmp_mashup.domain.usecase.base.BaseUseCase

/**
 * Determines the initial state of the app (Authenticated or Unauthenticated) on startup.
 */
enum class BootstrapResult {
    Authenticated, Unauthenticated
}

/**
 * Use case executed when the application starts.
 * Its main responsibility is to check if a valid session exists and update the app's authentication state.
 */
class BootstrapAppUseCase(
    private val tokenRepository: TokenRepository,
    private val authNotifier: AuthNotifier
) : BaseUseCase<Unit, BootstrapResult>() {

    /**
     * Executes the bootstrap logic.
     * - Checks for a refresh token.
     * - If it exists, attempts to refresh the session to validate it.
     * - Updates the global authentication state accordingly.
     */
    override suspend fun execute(params: Unit): Result<BootstrapResult> {
        val refreshToken = tokenRepository.getRefreshToken()

        return if (refreshToken.isNullOrBlank()) {
            // No refresh token, user is definitely not logged in.
            authNotifier.setUnauthenticated()
            Result.success(BootstrapResult.Unauthenticated)
        } else {
            // Refresh token exists, try to refresh the access token to validate the session.
            val refreshResult = tokenRepository.refreshAfterUnauthorized()
            
            if (refreshResult.isSuccess) {
                // Refresh successful, user is authenticated.
                authNotifier.setAuthenticated(userId = null)
                Result.success(BootstrapResult.Authenticated)
            } else {
                // Refresh failed (e.g., token expired), user must log in again.
                authNotifier.setUnauthenticated("Session expired")
                Result.success(BootstrapResult.Unauthenticated)
            }
        }
    }

    suspend operator fun invoke() = execute(Unit)
}
