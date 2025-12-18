package vn.dna.kmp_mashup.core.di

import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults
import vn.dna.kmp_mashup.core.service.NotificationServiceImpl
import vn.dna.kmp_mashup.domain.service.NotificationService

actual fun platformNetworkModule(): Module = module {
    single { Darwin.create() }
}

actual val platformStorageModule: Module = module {
    single<Settings> { // <--- Quan trọng: Ép kiểu về Settings interface
        val userDefaults = NSUserDefaults.standardUserDefaults
        NSUserDefaultsSettings(userDefaults)
    }
}

actual val platformServiceModule: Module = module {
    single<NotificationService> { NotificationServiceImpl() }
}
