package vn.dna.kmp_mashup.core.di.business

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.dna.kmp_mashup.domain.usecase.user.GetUserProfileUseCase
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "UserDIHelper", exact = true)
class UserDIHelper : KoinComponent {
    private val _getUserProfileUseCase: GetUserProfileUseCase by inject()

    fun getGetUserProfileUseCase(): GetUserProfileUseCase = _getUserProfileUseCase
}
