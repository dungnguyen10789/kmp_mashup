plugins {
    id("vn.dna.kmp.library")
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.touchlabSkie)
}

kotlin {
    // Truy cập lại các target iOS đã được plugin tạo để cấu hình Framework
    configure(listOf(iosArm64(), iosSimulatorArm64())) {
        binaries.framework {
            baseName = "MashupShared"
            isStatic = true

            // Export các module con ra ngoài framework để iOS dùng được
            export(projects.mobile.shared.domain)
            export(projects.mobile.shared.data)
            export(projects.mobile.shared.presentation)

            freeCompilerArgs += listOf(
                "-Xbinary=bundleId=vn.dna.kmp_mashup.shared"
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.mobile.shared.domain)
            api(projects.mobile.shared.data)
            api(projects.mobile.shared.presentation)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.multiplatform.settings)

            implementation(libs.bundles.ktor.common)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.security.crypto)
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "vn.dna.kmp_mashup.shared"
}
