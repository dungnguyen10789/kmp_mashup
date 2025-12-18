package vn.dna.kmp_mashup.presentation.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.dna.kmp_mashup.domain.auth.AuthNotifier
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Global App State Management Store.
 *
 * This class acts as the single source of truth for high-level application states
 * (e.g., Bootstrapping vs. Authenticated vs. Unauthenticated).
 *
 * It implements [AuthNotifier] to listen for authentication events triggered by the Domain layer
 * (e.g., when a token refresh fails, the Domain layer notifies this store to logout the user).
 *
 * It is designed to be consumed by both:
 * 1. Kotlin/Compose Multiplatform (via standard Flow APIs).
 * 2. iOS/Swift (via [watchState] and [watchEffects] helper methods).
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("AppStore")
class AppStore : AuthNotifier {
    private val scope: CoroutineScope = MainScope()

    // StateFlow holding the current route/state of the app.
    private val _appState = MutableStateFlow<AppState>(AppState.Bootstrapping)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    // SharedFlow for one-off events (Side Effects) like Toasts or Snackbars.
    private val _effects = MutableSharedFlow<AppEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<AppEffect> = _effects.asSharedFlow()

    /**
     * Transition the app to the Authenticated state.
     */
    override fun setAuthenticated(userId: String?) {
        _appState.value = AppState.Authenticated(userId)
    }

    /**
     * Transition the app to the Unauthenticated (Login) state.
     * Optionally triggers a UI message (e.g., "Session expired").
     */
    override fun setUnauthenticated(showMessage: String?) {
        _appState.value = AppState.Unauthenticated
        if (showMessage != null) {
            _effects.tryEmit(AppEffect.ShowMessage(showMessage))
        }
    }

    // Convenience overloads for Kotlin call sites
    fun setAuthenticated() = setAuthenticated(userId = null)
    fun setUnauthenticated() = setUnauthenticated(showMessage = null)

    override fun emitMessage(message: String) {
        _effects.tryEmit(AppEffect.ShowMessage(message))
    }

    // =========================================================================================
    // iOS / Swift INTEROP
    // =========================================================================================
    // The following methods facilitate observing Flows from Swift code,
    // as Swift does not natively support Kotlin Coroutines Flows directly without wrappers
    // (unless using a library like SKIE, which we are using, but manual wrappers are still safer for some cases).

    /** Swift-friendly cancellation handle to stop observing. */
    @ObjCName("Cancellable")
    interface Cancellable {
        fun cancel()
    }

    private class JobCancellable(private val job: Job) : Cancellable {
        override fun cancel() = job.cancel()
    }

    /**
     * Swift-friendly observer for AppState changes.
     */
    fun watchState(onEach: (AppState) -> Unit): Cancellable {
        val job = scope.launch(Dispatchers.Main.immediate) {
            appState.collect { onEach(it) }
        }
        return JobCancellable(job)
    }

    /**
     * Swift-friendly observer for One-Shot Effects.
     */
    fun watchEffects(onEach: (AppEffect) -> Unit): Cancellable {
        val job = scope.launch(Dispatchers.Main.immediate) {
            effects.collect { onEach(it) }
        }
        return JobCancellable(job)
    }
}
