@file:Suppress("UnstableApiUsage")

rootProject.name = "Mashup"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// =========================================================================================
// BUILD LOGIC (COMPOSITE BUILD)
// =========================================================================================
// Includes the 'buildLogic' project as a composite build.
// This allows us to define custom convention plugins (e.g., vn.dna.kmp.library) 
// that can be reused across all modules to enforce consistent build configurations.
includeBuild("buildLogic")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Needed for Compose Multiplatform dev builds
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Needed for Compose Multiplatform dev builds
    }
}

// =========================================================================================
// PROJECT STRUCTURE DEFINITION
// =========================================================================================

// --- BACKEND ---
// The Ktor server application. It shares the same repo to easily consume 'commonApi'.
include(":backend")
project(":backend").projectDir = file("backend")

// --- SHARED CONTRACT (The Glue) ---
// This is the most critical module for Code Sharing.
// It contains DTOs (Data Transfer Objects) and Interface definitions shared between
// the Backend (Provider) and the Mobile Clients (Consumers).
include(":commonApi")
project(":commonApi").projectDir = file("commonApi")

// --- MOBILE CLIENTS ---
// The entry point for the Android Application.
include(":mobile:android")
project(":mobile:android").projectDir = file("mobile/android")

// --- MOBILE SHARED LOGIC (KOTLIN MULTIPLATFORM) ---
// These modules follow Clean Architecture principles and are shared across Android & iOS.

// Core: Utilities, Extensions, and Common Infrastructure.
include(":mobile:shared:core")
project(":mobile:shared:core").projectDir = file("mobile/shared/core")

// Domain: Pure business logic, UseCases, and Domain Entities.
// This layer should have zero dependencies on Android frameworks or Data layers.
include(":mobile:shared:domain")
project(":mobile:shared:domain").projectDir = file("mobile/shared/domain")

// Data: Repository implementations, API networking, and Local caching.
include(":mobile:shared:data")
project(":mobile:shared:data").projectDir = file("mobile/shared/data")

// Presentation: UI state holders (ViewModels) and shared Compose Multiplatform UI components.
include(":mobile:shared:presentation")
project(":mobile:shared:presentation").projectDir = file("mobile/shared/presentation")
