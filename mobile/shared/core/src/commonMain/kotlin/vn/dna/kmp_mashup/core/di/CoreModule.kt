package vn.dna.kmp_mashup.core.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import vn.dna.kmp_mashup.data.cache.CurrentUserHolder
import vn.dna.kmp_mashup.data.db.DatabaseDriverFactory
import vn.dna.kmp_mashup.data.datasource.user.UserLocalDataSource
import vn.dna.kmp_mashup.data.network.HttpClientFactory
import vn.dna.kmp_mashup.data.network.gateway.RefreshTokenGatewayImpl
import vn.dna.kmp_mashup.data.repository.AuthRepositoryImpl
import vn.dna.kmp_mashup.data.repository.TokenRepositoryImpl
import vn.dna.kmp_mashup.data.repository.UserRepositoryImpl
import vn.dna.kmp_mashup.data.storage.KeyValueStorage
import vn.dna.kmp_mashup.data.storage.KeyValueStorageImpl
import vn.dna.kmp_mashup.data.storage.SecureStorage
import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import vn.dna.kmp_mashup.domain.config.AppConfig
import vn.dna.kmp_mashup.domain.config.AppConfigProvider
import vn.dna.kmp_mashup.domain.config.Environment
import vn.dna.kmp_mashup.domain.gateway.auth.RefreshTokenGateway
import vn.dna.kmp_mashup.domain.repository.auth.AuthRepository
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository
import vn.dna.kmp_mashup.domain.repository.user.UserRepository
import vn.dna.kmp_mashup.domain.usecase.auth.BootstrapAppUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LoginUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LogoutUseCase
import vn.dna.kmp_mashup.domain.usecase.user.GetMyProfileUseCase
import vn.dna.kmp_mashup.domain.usecase.user.GetUserProfileUseCase
import vn.dna.kmp_mashup.domain.usecase.user.ObserveMyProfileUseCase
import vn.dna.kmp_mashup.presentation.auth.AppStore
import vn.dna.kmp_mashup.presentation.viewmodel.auth.LoginViewModel

// Platform-specific module (OkHttp/Darwin engine)
expect fun platformNetworkModule(): Module

// Platform-specific storage module providing the 'Settings' instance
expect val platformStorageModule: Module

// Platform-specific service module (NotificationService)
expect val platformServiceModule: Module

// Common storage module that depends on 'Settings'
val commonStorageModule = module {
    single<KeyValueStorage> { KeyValueStorageImpl(settings = get()) }

    single { SecureStorage() }

    // --- Database & Cache Setup ---
    // 1. Platform-specific DB Driver
    single { DatabaseDriverFactory() }
    // 2. Local data source that uses the driver
    single { UserLocalDataSource(driverFactory = get()) }
    // 3. Application-wide CoroutineScope
    single<CoroutineScope>(named("AppScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    // 4. In-memory cache that observes the local data source
    single { CurrentUserHolder(localDataSource = get(), scope = get(named("AppScope"))) }
}

val dataModule = module {
    single { AppStore() }
    single<AuthNotifier> { get<AppStore>() }

    // --- Networking Setup ---

    // 1. Create "Public Client": This HttpClient does NOT have AuthInterceptor.
    // Used by RefreshTokenGateway to call the refresh token API without triggering infinite loops.
    single(named("publicClient")) {
        val engine: HttpClientEngine = get()
        HttpClientFactory(baseUrl = get<AppConfig>().baseUrl).createPublicClient(engine)
    }
    // 2. RefreshTokenGateway: Uses Public Client to perform token refreshing.
    // This acts as a bridge between TokenRepository and Network, preventing TokenRepository from directly depending on HttpClient.
    single<RefreshTokenGateway> {
        RefreshTokenGatewayImpl(publicClient = get(named("publicClient")))
    }
    // 3. TokenRepository: Manages tokens (storage, retrieval, refreshing).
    // Depends on RefreshTokenGateway to execute the refresh logic on the server side.
    single<TokenRepository> {
        TokenRepositoryImpl(
            storage = get<SecureStorage>(),
            authNotifier = get(),
            refreshTokenGateway = get()
        )
    }

    // Has AuthInterceptor, depends on TokenRepository to retrieve/refresh tokens for each request.
    // Dependency Structure: MainClient -> AuthInterceptor -> TokenRepository -> RefreshTokenGateway -> PublicClient
    // -> No Circular Dependency!
    single<HttpClient> {
        val engine: HttpClientEngine = get()
        val tokenRepository: TokenRepository = get()
        HttpClientFactory(baseUrl = get<AppConfig>().baseUrl).create(engine, tokenRepository)
    }
}

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get()
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            httpClient = get(),
            currentUserHolder = get()
        )
    }
}

val useCaseModule = module {
    factory { LoginUseCase(repository = get(), tokenRepository = get(), authNotifier = get()) }
    factory { LogoutUseCase(repository = get(), tokenRepository = get(), authNotifier = get()) }
    factory { BootstrapAppUseCase(tokenRepository = get(), authNotifier = get()) }
    
    // User UseCases
    factory { GetMyProfileUseCase(repository = get()) } // Fetches from network
    factory { GetUserProfileUseCase(repository = get()) } // Fetches other users
    factory { ObserveMyProfileUseCase(userRepository = get()) } // Observes from cache
}

val presentationModule = module {
    factory { LoginViewModel(loginUseCase = get()) }
}

fun appModules(environment: Environment): List<Module> {
    AppConfigProvider.init(environment)

    return listOf(
        module { single { AppConfigProvider.config } },
        platformNetworkModule(),
        platformStorageModule,
        commonStorageModule,
        platformServiceModule,
        dataModule,
        repositoryModule,
        useCaseModule,
        presentationModule
    )
}
