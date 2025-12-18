package vn.dna.kmp_mashup.core.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import vn.dna.kmp_mashup.data.storage.KeyValueStorageImpl
import vn.dna.kmp_mashup.domain.config.AppConfig
import vn.dna.kmp_mashup.domain.config.AppConfigProvider
import vn.dna.kmp_mashup.domain.config.Environment
import vn.dna.kmp_mashup.data.network.HttpClientFactory
import vn.dna.kmp_mashup.data.repository.AuthRepositoryImpl
import vn.dna.kmp_mashup.data.repository.TokenRepositoryImpl
import vn.dna.kmp_mashup.data.repository.UserRepositoryImpl
import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import vn.dna.kmp_mashup.domain.repository.auth.AuthRepository
import vn.dna.kmp_mashup.domain.repository.auth.TokenRepository
import vn.dna.kmp_mashup.domain.repository.user.UserRepository
import vn.dna.kmp_mashup.data.storage.KeyValueStorage
import vn.dna.kmp_mashup.domain.usecase.auth.BootstrapAppUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LoginUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LogoutUseCase
import vn.dna.kmp_mashup.domain.usecase.user.GetUserProfileUseCase
import vn.dna.kmp_mashup.presentation.auth.AppStore
import vn.dna.kmp_mashup.presentation.viewmodel.auth.LoginViewModel
import vn.dna.kmp_mashup.domain.service.NotificationService
import vn.dna.kmp_mashup.core.service.NotificationServiceImpl
import vn.dna.kmp_mashup.data.network.gateway.RefreshTokenGatewayImpl
import vn.dna.kmp_mashup.domain.gateway.auth.RefreshTokenGateway

// Platform-specific module (OkHttp/Darwin engine)
expect fun platformNetworkModule(): Module

// Platform-specific storage module providing the 'Settings' instance
expect val platformStorageModule: Module

// Platform-specific service module (NotificationService)
expect val platformServiceModule: Module

// Common storage module that depends on 'Settings'
val commonStorageModule = module {
    // This provides our abstracted storage service
    single<KeyValueStorage> { KeyValueStorageImpl(settings = get()) }
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
            storage = get(),
            authNotifier = get(),
            refreshTokenGateway = get()
        )
    }

    // 4. Main HttpClient: The primary client of the application.
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
    // Repositories now depend on HttpClient and KeyValueStorage (via TokenRepositoryImpl)
    single<AuthRepository> { AuthRepositoryImpl(httpClient = get()) }
    single<UserRepository> { UserRepositoryImpl(httpClient = get()) }
    // TokenRepository is already defined in dataModule, no need to redefine here.
    // single<TokenRepository> { ... } // Removed to avoid duplicate definition
}

val useCaseModule = module {
    // Use cases MUST only depend on domain interfaces (repositories, notifiers).
    factory { LoginUseCase(repository = get(), tokenRepository = get(), authNotifier = get()) }
    factory { LogoutUseCase(repository = get(), tokenRepository = get(), authNotifier = get()) }
    factory { GetUserProfileUseCase(repository = get()) }
    
    // Logic for app startup
    factory { BootstrapAppUseCase(tokenRepository = get(), authNotifier = get()) }
}

val presentationModule = module {
    factory { LoginViewModel(loginUseCase = get()) }
}

fun appModules(environment: Environment): List<Module> {
    AppConfigProvider.init(environment)

    return listOf(
        module { single { AppConfigProvider.config } },
        platformNetworkModule(),
        platformStorageModule, // Provides Settings
        commonStorageModule,   // Provides KeyValueStorage
        platformServiceModule, // Provides NotificationService (Platform Specific)
        dataModule,
        repositoryModule,
        useCaseModule,
        presentationModule
    )
}
