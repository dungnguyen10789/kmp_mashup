import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        androidMain.dependencies {
            // =========================================================================================
            // MODULE DEPENDENCIES
            // =========================================================================================
            // 'Presentation': Contains UI logic (ViewModels) and Screens. Essential for the App.
            implementation(projects.mobile.shared.presentation)
            // 'Core': Common utilities and extensions.
            implementation(projects.mobile.shared.core) 
            // 'Data': Required here for Dependency Injection (DI) initialization.
            // The App module must wire up the repositories to the viewmodels.
            implementation(projects.mobile.shared.data) 
            // 'Domain': Required to access UseCases and Entities.
            implementation(projects.mobile.shared.domain) 

            // =========================================================================================
            // COMPOSE UI DEPENDENCIES
            // =========================================================================================
            implementation(compose.preview)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // AndroidX Integration
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // DI (Koin)
            // Koin Android is needed for 'startKoin' and Android-specific scopes (Activity/Fragment)
            implementation(libs.koin.android)
            implementation(libs.koin.core)
        }
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "vn.dna.kmp_mashup"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "vn.dna.kmp_mashup"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    // =========================================================================================
    // BUILD FLAVORS (ENVIRONMENTS)
    // =========================================================================================
    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENV", "\"DEV\"")
            resValue("string", "app_name", "Mashup Dev")
        }
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "ENV", "\"STAGING\"")
            resValue("string", "app_name", "Mashup Staging")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "ENV", "\"PROD\"")
            resValue("string", "app_name", "Mashup")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
