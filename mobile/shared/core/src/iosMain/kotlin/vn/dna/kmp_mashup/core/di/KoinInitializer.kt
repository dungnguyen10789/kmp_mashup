package vn.dna.kmp_mashup.core.di

import vn.dna.kmp_mashup.domain.config.Environment
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "KoinInitializer", exact = true)
class KoinInitializer {
    fun start(envName: String) {
        val environment = try {
            Environment.valueOf(envName.uppercase())
        } catch (e: Exception) {
            Environment.DEV
        }
        initKoin(environment)
    }
}
