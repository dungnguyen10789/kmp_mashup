plugins {
    // =========================================================================================
    // ROOT PLUGIN CONFIGURATION
    // =========================================================================================
    // We apply plugins with 'apply false' here to load them onto the root classpath.
    // This ensures that all subprojects use the same version of these plugins, 
    // avoiding classpath conflicts and version mismatch errors.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.touchlabSkie) apply false
    
    // Apply the custom Architecture Check Plugin globally.
    // This plugin enforces Clean Architecture rules (e.g., Domain cannot depend on Data).
    id("vn.dna.kmp.arch.check")
}

// =========================================================================================
// GLOBAL LIFECYCLE HOOKS
// =========================================================================================
subprojects {
    // Hook the custom architecture check task into the standard 'check' lifecycle task.
    // This ensures that running './gradlew check' will also verify architecture rules.
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("archCheck"))
    }
}
