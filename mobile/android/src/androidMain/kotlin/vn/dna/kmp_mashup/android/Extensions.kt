package vn.dna.kmp_mashup.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Higher-Order Function (Extension) để xử lý pattern UseCase ném ngoại lệ.
 * Giúp code gọn gàng hơn, không cần lặp lại scope.launch và runCatching.
 */
fun <T> CoroutineScope.launchCatching(
    block: suspend () -> T,
    onSuccess: (T) -> Unit = {},
    onFailure: (Throwable) -> Unit = {},
    onLoading: (() -> Unit)? = null
) {
    launch {
        onLoading?.invoke()
        runCatching { block() }
            .onSuccess(onSuccess)
            .onFailure(onFailure)
    }
}
