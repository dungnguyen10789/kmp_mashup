package vn.dna.kmp_mashup.presentation.platform

import vn.dna.kmp_mashup.domain.model.error.FailureException // Import Custom Wrapper
import vn.dna.kmp_mashup.domain.model.error.Failure

/**
 * Find the root FailureException in the exception cause chain.
 *
 * This is useful because Kotlin/Native can wrap exceptions into other wrapper types.
 */
fun Throwable.findFailureException(): FailureException? {
    var currentCause: Throwable? = this

    // Walk the cause chain to find the original FailureException.
    while (currentCause != null) {

        // 1) Check by class name (more robust when `is` checks fail across boundaries)
        if (currentCause::class.simpleName == "FailureException") {
            // If the name matches, cast carefully.
            return currentCause as? FailureException
        }

        // 2) Move to next cause
        currentCause = currentCause.cause

        // Prevent infinite loops (if cause = this)
        if (currentCause == this) break
    }
    return null
}

/**
 * Convert any Throwable into a domain Failure model.
 */
fun Throwable.toFailure(): Failure {
    // 1) Try to unwrap FailureException
    val failureException = this.findFailureException()

    return if (failureException != null) {
        // If we found the wrapper, return the Failure inside.
        failureException.failure
    } else {
        // Otherwise, treat it as unknown (e.g., system or coding errors).
        Failure.UnknownError(this)
    }
}
