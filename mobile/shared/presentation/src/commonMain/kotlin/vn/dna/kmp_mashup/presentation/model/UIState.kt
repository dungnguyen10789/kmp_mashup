package vn.dna.kmp_mashup.presentation.model

import vn.dna.kmp_mashup.domain.model.error.Failure

/**
 * UiState: Sealed class chung cho mọi màn hình.
 * - T: Kiểu dữ liệu (Entity) mà màn hình này hiển thị (ví dụ: User, List<Product>).
 */
sealed class UIState<out T> {
    data object Loading : UIState<Nothing>()
    data class Success<out T>(val data: T) : UIState<T>()
    data class Error(val failure: Failure) : UIState<Nothing>()
    data object Idle : UIState<Nothing>()
}
