package vn.dna.kmp_mashup.presentation.viewmodel.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import vn.dna.kmp_mashup.domain.model.auth.LoginCredentials
import vn.dna.kmp_mashup.domain.usecase.auth.LoginUseCase
import vn.dna.kmp_mashup.presentation.navigation.NavEffect
import vn.dna.kmp_mashup.presentation.viewmodel.base.BaseViewModel

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
) : BaseViewModel() {

    private val _effects = MutableSharedFlow<NavEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<NavEffect> = _effects

    /**
     * Swift-friendly login API.
     * - Uses [scope] to execute the suspend use case.
     * - Returns result via callbacks (bridges naturally to Swift blocks).
     */
    fun login(
        username: String,
        password: String,
        onSuccess: (userId: String) -> Unit,
        onError: (message: String) -> Unit,
    ) {
        scope.launch {
            try {
                // invoke now returns R directly, or throws exception on failure
                loginUseCase.invoke(
                    LoginCredentials(
                        username = username,
                        password = password
                    )
                )
                // Success path
                val userId = username
                onSuccess(userId)
                _effects.emit(NavEffect.NavigateToHome(userId))
            } catch (t: Throwable) {
                // Failure path
                onError(t.message ?: "Login failed")
            }
        }
    }

    fun onBackClicked() {
        scope.launch {
            _effects.emit(NavEffect.Back)
        }
    }
}
