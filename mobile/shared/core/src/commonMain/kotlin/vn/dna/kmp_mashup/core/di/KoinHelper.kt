package vn.dna.kmp_mashup.core.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import vn.dna.kmp_mashup.domain.config.Environment

/**
 * Initializes the Dependency Injection (DI) framework for the KMP application.
 *
 * This function is the entry point for starting Koin in both Android and iOS applications.
 *
 * @param environment The current runtime environment (e.g., [Environment.DEV], [Environment.PROD]).
 *                    This is used to configure environment-specific modules (like base URLs).
 * @param appDeclaration An optional lambda for platform-specific Koin configuration.
 *                       - On Android: Pass `androidContext(this)` here.
 *                       - On iOS: Pass empty or iOS-specific logger config.
 */
fun initKoin(environment: Environment, appDeclaration: KoinApplication.() -> Unit = {}) {
    val koinApplication = startKoin {
        appDeclaration()
        modules(appModules(environment))
    }
    // Initialize KoinHolder so it can be accessed safely elsewhere (e.g., from Swift)
    KoinHolder.koin = koinApplication.koin
}
