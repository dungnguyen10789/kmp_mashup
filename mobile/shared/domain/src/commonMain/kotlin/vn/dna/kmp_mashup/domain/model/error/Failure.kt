package vn.dna.kmp_mashup.domain.model.error

/**
 * Failure: Sealed data model representing business/domain failures that the UI and domain
 * layer can handle.
 *
 * This is NOT an Exception type.
 */
sealed class Failure(open val message: String? = null) {

    // API error (HTTP status codes)
    data class ApiError(val code: Int, val errorBody: String? = null, override val message: String? = "API Error: $code") : Failure(message)

    // Network error (no connection, timeout, etc.)
    data object NetworkError : Failure("Network connection failed.")

    // Data parsing/mapping error (serialization/mapping)
    // Keep the original Throwable for debugging/logging.
    data class DataMappingError(val exception: Throwable) : Failure("Data mapping failed.")

    // Unknown/unexpected error
    data class UnknownError(val exception: Throwable? = null) : Failure("An unknown error occurred.")
}
