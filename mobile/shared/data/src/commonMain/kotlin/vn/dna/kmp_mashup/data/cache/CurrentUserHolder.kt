package vn.dna.kmp_mashup.data.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import vn.dna.kmp_mashup.data.datasource.user.UserLocalDataSource
import vn.dna.kmp_mashup.domain.entity.user.UserEntity

/**
 * A Singleton class holding the Current User Profile in memory.
 *
 * **Mechanism (SSOT):**
 * - It observes [UserLocalDataSource.observeUser].
 * - Whenever the DB changes, this StateFlow updates automatically.
 * - UI/UseCases observe this [userFlow].
 */
class CurrentUserHolder(
    localDataSource: UserLocalDataSource,
    scope: CoroutineScope // Application Scope to keep the subscription alive
) {
    /**
     * A Hot Flow that always holds the latest user from DB.
     */
    val userFlow: StateFlow<UserEntity?> = localDataSource.observeUser() // Changed from getUserStream
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    /**
     * Get the current value synchronously (Snapshot).
     */
    fun getSnapshot(): UserEntity? = userFlow.value
}
