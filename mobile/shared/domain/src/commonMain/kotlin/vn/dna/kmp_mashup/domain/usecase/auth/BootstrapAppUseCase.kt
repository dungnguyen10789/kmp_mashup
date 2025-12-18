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
     * 
     * Improved Logic for Chat App:
     * 1. Checks if a valid Access Token exists (RAM or Disk).
     * 2. If yes, considers Authenticated immediately (No Network Call).
     * 3. If no (expired/missing), attempts to refresh.
     * 4. If refresh fails, considers Unauthenticated.
     */
    override suspend fun execute(params: Unit): Result<BootstrapResult> {
        // ensureValidAccessToken() handles the "Smart Check":
        // - Checks SecureStorage first.
        // - If valid -> returns token (Fast start, No API).
        // - If expired -> calls API Refresh Token.
        val accessToken = tokenRepository.ensureValidAccessToken()

        return if (accessToken != null) {
            // Valid session found (either from cache or fresh refresh)
            authNotifier.setAuthenticated(userId = null)
            Result.success(BootstrapResult.Authenticated)
        } else {
            // No valid session could be established
            authNotifier.setUnauthenticated()
            Result.success(BootstrapResult.Unauthenticated)
        }
    }

    suspend operator fun invoke() = execute(Unit)
}
