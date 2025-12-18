package vn.dna.kmp_mashup.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class DatabaseDriverFactory : KoinComponent {
    private val context: Context by inject()

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(CoreDatabase.Schema, context, "core.db")
    }
}
