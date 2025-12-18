plugins {
    // We use the standard Kotlin JVM plugin here instead of Multiplatform
    // because the backend runs strictly on the JVM environment.
    kotlin("jvm")
    alias(libs.plugins.ktor)
    application
}

// Group and Version are crucial for Backend deployment artifacts (JARs, Docker images).
group = "vn.dna.kmp_mashup"
version = "0.0.1"

application {
    mainClass.set("vn.dna.kmp_mashup.backend.ApplicationKt")
    
    // Pass the development flag to Ktor based on Gradle project properties.
    // Run with `./gradlew run -Pdevelopment=true` to enable hot reload.
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // Ktor Server Core & Netty Engine
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    
    // Serialization (JSON)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    
    // Monitoring & Auth
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    
    // Database Layer (Exposed ORM + PostgreSQL)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    // Logging
    implementation(libs.logback.classic)
    
    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
