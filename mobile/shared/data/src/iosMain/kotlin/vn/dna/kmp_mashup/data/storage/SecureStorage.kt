package vn.dna.kmp_mashup.data.storage

import kotlinx.cinterop.*
import org.koin.core.component.KoinComponent
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual class SecureStorage : KoinComponent {
    private val service = "vn.dna.kmp_mashup.secure"

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getString(key: String): String? = memScoped {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        val result = alloc<CFTypeRefVar>()
        
        val status = SecItemCopyMatching(query as CFDictionaryRef?, result.ptr)

        if (status == errSecSuccess) {
            val data = result.value
            val nsData = CFBridgingRelease(data) as? NSData
            
            return@memScoped nsData?.let {
                // Use NSString.create instead of alloc().initWithData
                // This correctly maps to the Objective-C initializer initWithData:encoding:
                NSString.create(data = it, encoding = NSUTF8StringEncoding) as String?
            }
        }
        return@memScoped null
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun putString(key: String, value: String) {
        // Explicitly cast Kotlin String to NSString to access dataUsingEncoding
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        remove(key)

        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecValueData to data
        )

        SecItemAdd(query as CFDictionaryRef?, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun remove(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key
        )
        SecItemDelete(query as CFDictionaryRef?)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun clear() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service
        )
        SecItemDelete(query as CFDictionaryRef?)
    }
}
