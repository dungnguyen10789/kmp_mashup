package vn.dna.kmp_mashup.data.storage

/**
 * An abstraction over a simple key-value data store.
 * This interface belongs to the **Data Layer** because it defines a contract for a data source,
 * but its implementation is provided by a specific library (Multiplatform-Settings).
 * It's not in the domain layer because the domain shouldn't be concerned with how data is stored,
 * only that it *can* be stored.
 */
interface KeyValueStorage {
    fun getString(key: String, defaultValue: String? = null): String?
    fun putString(key: String, value: String)

    fun getInt(key: String, defaultValue: Int = 0): Int
    fun putInt(key: String, value: Int)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)

    fun remove(key: String)
    fun clear()
}
