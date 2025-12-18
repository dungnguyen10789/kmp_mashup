plugins {
    // Applies the shared KMP configuration (Android & iOS targets, Java 17, etc.)
    id("vn.dna.kmp.library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    // The Convention Plugin ("vn.dna.kmp.library") already sets up Android & iOS targets.
    // We explicitly add the JVM target here because this module is also consumed by the Backend.
    // Backend (Ktor) runs on JVM and will import this module as a standard .jar library.
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            
            // Ktor dependencies are included here to allow sharing HTTP logic or
            // serialization configuration between Client and Server.
            implementation(libs.bundles.ktor.common)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
    }
}

android {
    // 'namespace' is specific to Android build system.
    // It is used to generate the R class (e.g. vn.dna.kmp_mashup.common_api.R) for Android Resources.
    // Note: The Backend (JVM) ignores this. It imports classes based on the 'package' defined in Kotlin files.
    namespace = "vn.dna.kmp_mashup.common_api"
    // CompileSdk, MinSdk, and other Android defaults are handled by the Convention Plugin.
}
