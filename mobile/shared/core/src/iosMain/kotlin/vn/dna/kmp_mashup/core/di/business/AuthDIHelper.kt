package vn.dna.kmp_mashup.core.di.business

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.dna.kmp_mashup.domain.usecase.auth.LoginUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LogoutUseCase
import vn.dna.kmp_mashup.presentation.viewmodel.auth.LoginViewModel
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "AuthDIHelper", exact = true)
class AuthDIHelper : KoinComponent {
    private val _loginUseCase: LoginUseCase by inject()
    private val _logoutUseCase: LogoutUseCase by inject()
    private val _loginViewModel: LoginViewModel by inject()

    fun getLoginUseCase(): LoginUseCase = _loginUseCase
    fun getLogoutUseCase(): LogoutUseCase = _logoutUseCase
    fun getLoginViewModel(): LoginViewModel = _loginViewModel
}
