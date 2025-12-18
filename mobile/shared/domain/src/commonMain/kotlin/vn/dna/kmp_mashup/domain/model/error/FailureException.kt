package vn.dna.kmp_mashup.domain.model.error

/**
 * FailureException: Exception wrapper for returning domain failures through Result.failure().
 *
 * It holds a Failure model inside and can be caught by repository error handling.
 */
class FailureException(val failure: Failure) : Exception(failure.message)
