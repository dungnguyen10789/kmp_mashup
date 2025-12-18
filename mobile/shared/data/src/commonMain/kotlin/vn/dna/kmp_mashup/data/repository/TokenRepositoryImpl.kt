package vn.dna.kmp_mashup.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import vn.dna.kmp_mashup.domain.entity.auth.TokenEntity
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository
import vn.dna.kmp_mashup.data.storage.SecureStorage
import vn.dna.kmp_mashup.domain.gateway.auth.RefreshTokenGateway
import kotlin.concurrent.Volatile
import io.ktor.util.decodeBase64Bytes
import kotlinx.datetime.Clock

/**
 * Concrete implementation of [TokenRepository].
 *
 * This repository is the Single Source of Truth for authentication tokens in the app.
 * It manages:
 * 1. **In-Memory Caching**: Keeps the Access Token in memory for fast access and security (avoiding frequent disk reads).
 * 2. **Secure Persistence**: Stores the Refresh Token persistently using [SecureStorage] (EncryptedSharedPreferences on Android, Keychain on iOS).
 * 3. **Token Refresh Logic**: Implements "Single-Flight" logic to ensure that if multiple API calls fail with 401 simultaneously,
 *    only ONE refresh request is sent to the server.
 */
class TokenRepositoryImpl(
    private val storage: SecureStorage,
    private val authNotifier: AuthNotifier,
    private val refreshTokenGateway: RefreshTokenGateway 
) : TokenRepository, BaseRepositoryImpl() {

    @Volatile
    private var inMemoryAccessToken: String? = null

    private val refreshMutex = Mutex()
    private var inFlightRefresh: Deferred<Result<TokenEntity>>? = null

    companion object {
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }

    override suspend fun getAccessToken(): String? = inMemoryAccessToken

    override suspend fun getRefreshToken(): String? = storage.getString(KEY_REFRESH_TOKEN)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        inMemoryAccessToken = accessToken
        storage.putString(KEY_REFRESH_TOKEN, refreshToken)
    }

    override suspend fun clearTokens() {
        inMemoryAccessToken = null
        storage.remove(KEY_REFRESH_TOKEN)
    }

    override suspend fun ensureValidAccessToken(): String? {
        val current = inMemoryAccessToken
        if (current != null && !isAccessTokenExpired(current)) return current

        val refreshToken = getRefreshToken() ?: return null
        return refreshSingleFlight(refreshToken).getOrNull()?.accessToken
    }

    override suspend fun refreshAfterUnauthorized(): Result<TokenEntity> {
        val refreshToken = getRefreshToken() ?: return Result.failure(IllegalStateException("No refresh token"))
        return refreshSingleFlight(refreshToken)
    }

    private suspend fun refreshSingleFlight(refreshToken: String): Result<TokenEntity> = coroutineScope {
        val job = refreshMutex.withLock {
            inFlightRefresh ?: async {
                refreshTokenGateway.refresh(refreshToken)
            }.also { inFlightRefresh = it }
        }

        try {
            val result = job.await()
            result.onSuccess { tokens ->
                saveTokens(tokens.accessToken, tokens.refreshToken)
                authNotifier.setAuthenticated(userId = null)
            }.onFailure { throwable ->
                if (isInvalidRefreshToken(throwable)) {
                    clearTokens()
                    authNotifier.setUnauthenticated("Session expired")
                }
            }
            result
        } finally {
            refreshMutex.withLock {
                if (inFlightRefresh == job) inFlightRefresh = null
            }
        }
    }

    private fun isInvalidRefreshToken(t: Throwable): Boolean {
        return (t as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
    }

    private fun isAccessTokenExpired(accessToken: String): Boolean {
        try {
            val payload = accessToken.split(".")[1]
            val decoded = payload.decodeBase64Bytes()
            val json = decoded.decodeToString()
            val map = Json.decodeFromString<Map<String, Any>>(json)
            val exp = map["exp"] as? Long ?: return true

            // KMP time: Clock.System.now().epochSeconds
            val currentEpoch = Clock.System.now().epochSeconds
            return currentEpoch >= exp - 300 // Refresh 5 minutes exp
        } catch (e: Exception) {
            return true // Parse failed → assume as expired to safety refresh
        }
    }
}
