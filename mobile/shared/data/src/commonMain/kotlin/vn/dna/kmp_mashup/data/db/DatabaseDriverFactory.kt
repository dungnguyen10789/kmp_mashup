package vn.dna.kmp_mashup.data.db

import app.cash.sqldelight.db.SqlDriver

// Changed from internal to public to be visible to other modules (like DI in :core)
expect class DatabaseDriverFactory() {
    fun createDriver(): SqlDriver
}
