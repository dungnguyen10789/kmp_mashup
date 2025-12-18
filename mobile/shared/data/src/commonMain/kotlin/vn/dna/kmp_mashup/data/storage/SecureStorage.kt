package vn.dna.kmp_mashup.data.storage

/**
 * Interface expectation for Secure Storage.
 *
 * This class provides a common interface for accessing platform-specific secure storage mechanisms
 * (EncryptedSharedPreferences on Android, Keychain on iOS).
 *
 * It must have a default constructor exposed to be instantiated via Koin in common code.
 */
expect class SecureStorage() { // Added parentheses () to expose the constructor
    suspend fun getString(key: String): String?
    suspend fun putString(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}
