plugins {
    `kotlin-dsl`
}

group = "vn.dna.kmp_mashup.buildlogic"

dependencies {
    // Compile-only dependencies for writing plugins
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "vn.dna.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("archCheck") {
            id = "vn.dna.kmp.arch.check"
            implementationClass = "ArchCheckConventionPlugin"
        }
    }
}
