package vn.dna.kmp_mashup.data.storage

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class SecureStorage : KoinComponent {
    private val context: Context by inject()

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    actual suspend fun getString(key: String): String? = prefs.getString(key, null)

    actual suspend fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual suspend fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual suspend fun clear() {
        prefs.edit().clear().apply()
    }
}