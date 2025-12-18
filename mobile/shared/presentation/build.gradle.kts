plugins {
    id("vn.dna.kmp.library")
}

kotlin {
    // Không cần setup target, plugin đã lo
    sourceSets {
        commonMain.dependencies {
            api(projects.mobile.shared.domain)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "vn.dna.kmp_mashup.shared.presentation"
}
