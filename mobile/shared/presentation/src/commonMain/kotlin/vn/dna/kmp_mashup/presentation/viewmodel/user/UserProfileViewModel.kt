package vn.dna.kmp_mashup.presentation.viewmodel.user

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vn.dna.kmp_mashup.domain.entity.user.UserEntity
import vn.dna.kmp_mashup.domain.model.error.Failure
import vn.dna.kmp_mashup.domain.usecase.user.GetUserProfileUseCase
import vn.dna.kmp_mashup.presentation.model.UIState
import vn.dna.kmp_mashup.presentation.platform.toFailure
import vn.dna.kmp_mashup.presentation.viewmodel.base.BaseViewModel

class UserProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow<UIState<UserEntity>>(UIState.Idle)
    val uiState: StateFlow<UIState<UserEntity>> = _uiState

    private fun loadUserProfile() {
        scope.launch {
            _uiState.value = UIState.Loading

            try {
                // 1) Call use case (returns UserEntity directly)
                val user = getUserProfileUseCase.invoke()

                // If successful, emit UiState.Success
                _uiState.value = UIState.Success(user)
            } catch (throwable: Throwable) {
                // 2) Handle Error
                val failure = throwable.toFailure()

                // Special case: session expired
                if (failure is Failure.ApiError && failure.code == 401) {
                    // Example: navigate to login screen, clear tokens
                    // ViewModel should typically only deal with presentation/navigation concerns.
                }

                // Expose error state to UI
                _uiState.value = UIState.Error(failure)
            }
        }
    }
}
