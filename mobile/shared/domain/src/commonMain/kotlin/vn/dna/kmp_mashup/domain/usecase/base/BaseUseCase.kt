package vn.dna.kmp_mashup.domain.usecase.base

/**
 * Abstract base class for all Use Cases in the Clean Architecture Domain layer.
 *
 * A Use Case represents a single unit of business logic (e.g., "Login", "GetProfile").
 * It enforces a consistent execution pattern and error handling strategy.
 *
 * @param P The type of the input parameters (Input Port). Use [Unit] if no input is needed.
 * @param R The type of the return value (Output Port).
 */
abstract class BaseUseCase<in P, out R> {

    /**
     * Executes the business logic.
     *
     * This function wraps the internal [execute] call and unwraps the [Result].
     * Ideally, the Presentation layer should handle the `Result` wrapper, but this
     * invoke operator simplifies usage for cases where we expect a direct value or exception throw.
     *
     * @param params The input parameters.
     * @return The execution result of type [R].
     * @throws Exception If the execution fails.
     */
    @Throws(Exception::class)
    suspend operator fun invoke(params: P): R {
        return execute(params).getOrThrow()
    }

    /**
     * The core logic implementation.
     *
     * Subclasses must implement this method to define the specific business rules.
     * It should catch expected errors and wrap them in [Result.failure].
     *
     * @param params The input parameters.
     * @return A [Result] encapsulating either success with data [R] or failure.
     */
    protected abstract suspend fun execute(params: P): Result<R>
}
