package vn.dna.kmp_mashup.domain.config

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Data class holding all application configurations.
 */
data class AppConfig(
    val environment: Environment,
    val baseUrl: String,
)

/**
 * A singleton provider for the application configuration.
 * It must be initialized once at startup using `init()`.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("AppConfigProvider", exact = true)
object AppConfigProvider {
    private var _config: AppConfig? = null

    val config: AppConfig
        get() = checkNotNull(_config) {
            "AppConfigProvider has not been initialized. Call init(environment) first."
        }

    fun init(environment: Environment) {
        if (_config != null) {
            // Log or handle re-initialization if necessary, but for now we ignore.
            return
        }
        // The base URL MUST end with a trailing slash for Ktor's URL resolution to work correctly.
        _config = when (environment) {
            Environment.DEV -> AppConfig(
                environment = environment,
                baseUrl = "http://localhost:4201/api/", // Correct URL with trailing slash
            )
            Environment.STAGING -> AppConfig(
                environment = environment,
                baseUrl = "http://localhost:4201/api/", // Correct URL with trailing slash
            )
            Environment.PROD -> AppConfig(
                environment = environment,
                baseUrl = "http://localhost:4201/api/", // Correct URL with trailing slash
            )
        }
    }

    // --- Convenience accessors for Swift/Objective-C ---
    val environmentName: String get() = config.environment.name
    val baseUrl: String get() = config.baseUrl
}
