package vn.dna.kmp_mashup.data.repository

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import vn.dna.kmp_mashup.domain.model.error.Failure
import vn.dna.kmp_mashup.domain.model.error.FailureException
import kotlinx.serialization.SerializationException
import vn.dna.kmp_mashup.dto.base.ApiResponseDTO

/**
 * Abstract Base Repository Implementation.
 *
 * This class provides a centralized mechanism for handling network requests ([safeApiCall]).
 * It automatically catches common exceptions (3xx, 4xx, 5xx, Parsing) and maps them into
 * Domain-specific [FailureException] types.
 *
 * All Repositories in the Data layer should extend this class to ensure consistent error handling.
 */
abstract class BaseRepositoryImpl {

    /**
     * Executes a network call safely within a try-catch block and wraps the result.
     *
     * It expects the API to return a standard envelope structure [ApiResponseDTO].
     *
     * @param T The expected type of the  `data` field in the API response.
     * @param apiCall A lambda representing the suspending Ktor network request.
     * @return A [Result] containing the parsed data [T] or a mapped [FailureException].
     */
    protected suspend inline fun <reified T> safeApiCall(
        apiCall: () -> HttpResponse
    ): Result<T> {
        return try {
            val response = apiCall()
            
            // Optimization: If the caller expects Unit, we ignore the body parsing.
            if (T::class == Unit::class) {
                return Result.success(Unit as T)
            }

            // Parse the standard JSON envelope
            val apiResponse: ApiResponseDTO<T> = response.body()
            val data = apiResponse.data

            if (data != null) {
                Result.success(data)
            } else {
                // Determine if this is truly an error or just unexpected empty data
                val e = Exception("API returned success but data is null. Status: ${apiResponse.statusCode}, Message: ${apiResponse.message}")
                println("BaseRepositoryImpl: DataNullError: $e")
                Result.failure(FailureException(Failure.DataMappingError(e)))
            }
        } catch (e: RedirectResponseException) {
            println("RedirectResponseException $e")
            // Handle 3xx redirects
            Result.failure(mapToFailure(e))
        } catch (e: ClientRequestException) {
            println("ClientRequestException $e")
            // Handle 4xx client errors (e.g., 401 Unauthorized, 404 Not Found)
            Result.failure(mapToFailure(e))
        } catch (e: ServerResponseException) {
            println("ServerResponseException $e")
            // Handle 5xx server errors
            Result.failure(mapToFailure(e))
        } catch (e: SerializationException) {
            println("SerializationException $e")
            // Handle JSON parsing errors (mismatch between DTO and JSON)
            Result.failure(FailureException(Failure.DataMappingError(e)))
        } catch (e: Exception) {
            println("UnknownException $e")
            Result.failure(FailureException(Failure.UnknownError(e)))
        }
    }

    /**
     * Maps low-level Ktor exceptions to high-level Domain failures.
     */
    protected fun mapToFailure(e: Exception): FailureException {
        println("mapToFailure $e")
        return when (e) {
            is ClientRequestException -> {
                FailureException(Failure.ApiError(
                    code = e.response.status.value,
                    errorBody = "" // TODO: Read error body asynchronously if needed
                ))
            }
            is ServerResponseException -> {
                FailureException(Failure.ApiError(
                    code = e.response.status.value
                ))
            }
            else -> FailureException(Failure.UnknownError(e))
        }
    }
}
