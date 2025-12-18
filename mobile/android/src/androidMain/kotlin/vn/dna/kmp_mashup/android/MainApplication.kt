package vn.dna.kmp_mashup.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import vn.dna.kmp_mashup.domain.config.Environment
import vn.dna.kmp_mashup.core.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // FIX: Pass the environment variable from BuildConfig
        initKoin(environment = Environment.DEV) {
            androidContext(this@MainApplication)
        }
    }
}
