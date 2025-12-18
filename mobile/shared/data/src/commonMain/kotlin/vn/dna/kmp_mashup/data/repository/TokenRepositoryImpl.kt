package vn.dna.kmp_mashup.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
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
 * 1. **In-Memory Caching**: Keeps the Access Token in memory for fast access.
 * 2. **Secure Persistence**: Stores BOTH Refresh Token AND Access Token persistently using [SecureStorage].
 *    This optimizes app startup for Chat Apps by avoiding unnecessary refresh calls if the Access Token is still valid.
 * 3. **Token Refresh Logic**: Implements "Single-Flight" logic.
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
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        
        // Buffer time to refresh token before it actually expires.
        // Reduced to 15s to avoid unnecessary refreshes during cold start or shortly after login.
        private const val EXPIRATION_BUFFER_SECONDS = 15
    }

    override suspend fun getAccessToken(): String? {
        // Try memory first, then fallback to storage (and cache it if found)
        return inMemoryAccessToken ?: storage.getString(KEY_ACCESS_TOKEN)?.also {
            inMemoryAccessToken = it
        }
    }

    override suspend fun getRefreshToken(): String? = storage.getString(KEY_REFRESH_TOKEN)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        inMemoryAccessToken = accessToken
        // Save BOTH tokens to SecureStorage
        storage.putString(KEY_ACCESS_TOKEN, accessToken)
        storage.putString(KEY_REFRESH_TOKEN, refreshToken)
    }

    override suspend fun clearTokens() {
        inMemoryAccessToken = null
        // Remove BOTH tokens
        storage.remove(KEY_ACCESS_TOKEN)
        storage.remove(KEY_REFRESH_TOKEN)
    }

    /**
     * Ensures a valid access token exists.
     * Strategy:
     * 1. Check Memory.
     * 2. Check Storage (Optimized for App Restart).
     * 3. If invalid/expired -> Call Refresh Token API.
     */
    override suspend fun ensureValidAccessToken(): String? {
        // 1. Check Memory
        var current = inMemoryAccessToken
        if (current != null && !isAccessTokenExpired(current)) return current

        // 2. Check Storage (Fast Path: App Restart)
        current = storage.getString(KEY_ACCESS_TOKEN)
        if (current != null && !isAccessTokenExpired(current)) {
            // Found valid token in storage -> Load to memory and return immediately.
            // No network call needed!
            inMemoryAccessToken = current
            return current
        }

        // 3. Fallback: Token missing or expired -> Must refresh via Network
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
            val jsonString = decoded.decodeToString()
            
            // Use JsonObject/JsonElement parsing (Safe)
            val jsonElement = Json.parseToJsonElement(jsonString)
            val jsonObject = jsonElement.jsonObject
            
            val exp = jsonObject["exp"]?.jsonPrimitive?.doubleOrNull?.toLong() ?: return true

            val currentEpoch = Clock.System.now().epochSeconds
            return currentEpoch >= exp - EXPIRATION_BUFFER_SECONDS
        } catch (e: Exception) {
            println("TokenRepository: Check Expiration Failed: $e")
            return true 
        }
    }
}
