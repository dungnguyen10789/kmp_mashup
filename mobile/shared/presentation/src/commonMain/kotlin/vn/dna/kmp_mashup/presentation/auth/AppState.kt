package vn.dna.kmp_mashup.presentation.auth

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("AppState")
sealed interface AppState {
    data object Bootstrapping : AppState
    data object Unauthenticated : AppState
    data class Authenticated(val userId: String? = null) : AppState
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("AppEffect")
sealed interface AppEffect {
    /**
     * One-shot effect. UI may show an alert/toast.
     */
    data class ShowMessage(val message: String) : AppEffect
}
