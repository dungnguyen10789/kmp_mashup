package vn.dna.kmp_mashup.core.di.business

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.dna.kmp_mashup.data.storage.KeyValueStorage
import vn.dna.kmp_mashup.domain.usecase.auth.BootstrapAppUseCase
import vn.dna.kmp_mashup.presentation.auth.AppStore
import vn.dna.kmp_mashup.domain.service.NotificationService
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "CoreDIHelper", exact = true)
class CoreDIHelper : KoinComponent {
    private val _bootstrapAppUseCase: BootstrapAppUseCase by inject()
    private val _keyValueStorage: KeyValueStorage by inject()
    private val _appStore: AppStore by inject()
    private val _notificationService: NotificationService by inject()

    fun getBootstrapAppUseCase(): BootstrapAppUseCase = _bootstrapAppUseCase
    fun getKeyValueStorage(): KeyValueStorage = _keyValueStorage
    fun getAppStore(): AppStore = _appStore
    fun getNotificationService(): NotificationService = _notificationService
}
