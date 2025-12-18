package vn.dna.kmp_mashup.presentation.viewmodel.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class BaseViewModel {
    protected val scope: CoroutineScope = MainScope()

    open fun onCleared() {
        scope.cancel()
    }
}
