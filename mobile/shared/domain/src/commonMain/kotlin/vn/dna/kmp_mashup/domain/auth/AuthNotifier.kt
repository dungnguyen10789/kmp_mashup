package vn.dna.kmp_mashup.domain.auth

/**
 * Domain-level abstraction for auth routing notifications.
 *
 * Data layer (TokenManager/AuthBootstrapper) can depend on this interface without
 * importing presentation-layer types.
 */
interface AuthNotifier {
    fun setAuthenticated(userId: String? = null)
    fun setUnauthenticated(showMessage: String? = null)
    fun emitMessage(message: String)
}
