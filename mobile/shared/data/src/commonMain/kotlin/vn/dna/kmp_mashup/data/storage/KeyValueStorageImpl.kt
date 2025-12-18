package vn.dna.kmp_mashup.data.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Concrete implementation of [KeyValueStorage] using the `Multiplatform Settings` library.
 *
 * This class acts as a wrapper around the platform-specific settings implementation
 * (SharedPreferences on Android, NSUserDefaults on iOS).
 *
 * @param settings The underlying settings instance provided by the platform module via DI.
 */
class KeyValueStorageImpl(
    private val settings: Settings
) : KeyValueStorage {

    override fun getString(key: String, defaultValue: String?): String? {
        return settings.getStringOrNull(key) ?: defaultValue
    }

    override fun putString(key: String, value: String) {
        settings[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return settings.getInt(key, defaultValue)
    }

    override fun putInt(key: String, value: Int) {
        settings[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        settings[key] = value
    }

    override fun remove(key: String) {
        settings.remove(key)
    }

    override fun clear() {
        settings.clear()
    }
}
