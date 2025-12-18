@file:JvmName("AndroidCoreModule")
package vn.dna.kmp_mashup.core.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings
import android.content.Context
import vn.dna.kmp_mashup.core.service.NotificationServiceImpl
import vn.dna.kmp_mashup.domain.service.NotificationService

actual fun platformNetworkModule(): Module = module {
    single<HttpClientEngine> { OkHttp.create() }
}

actual val platformStorageModule: Module = module {
    single<Settings> { // Explicitly bind to Settings interface
        val context: Context = androidContext()
        val sharedPreferences = context.getSharedPreferences("mashup_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPreferences)
    }
}

actual val platformServiceModule: Module = module {
    single<NotificationService> { NotificationServiceImpl(context = androidContext()) }
}
