package vn.dna.kmp_mashup.dto.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Generic API response envelope.
 *
 * Backend format:
 * {
 *   "statusCode": 0,
 *   "message": "Success",
 *   "data": { ... },
 *   "error": null,
 *   "timestamp": "...",
 *   "path": "/api/..."
 * }
 */
@Serializable
data class ApiResponseDTO<T>(
    @SerialName("statusCode") val statusCode: Int,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null,
    @SerialName("error") val error: JsonElement? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("path") val path: String? = null,
)