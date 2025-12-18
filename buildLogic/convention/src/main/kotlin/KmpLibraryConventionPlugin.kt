import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention Plugin for Shared KMP Libraries.
 *
 * This plugin centralizes the configuration for all Kotlin Multiplatform libraries
 * in the 'mobile/shared' group. Instead of repeating Android SDK versions,
 * JDK compatibility, and iOS target definitions in every build.gradle.kts file,
 * we define them once here.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply core plugins required for any KMP Android Library
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
            }

            // Standardize Android Library settings
            extensions.configure<LibraryExtension> {
                compileSdk = 36 // Use the latest SDK for compilation
                defaultConfig {
                    minSdk = 24 // Minimum supported Android version
                }
                compileOptions {
                    // Enforce Java 17 compatibility across the board
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            // Standardize Kotlin Multiplatform Target settings
            extensions.configure<KotlinMultiplatformExtension> {
                // Configure Android Target
                androidTarget {
                    publishLibraryVariants("release", "debug")
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }
                }
                
                // Configure iOS Targets
                // We include all standard architectures to support both Simulators and Real Devices
                iosX64()
                iosArm64()
                iosSimulatorArm64()
            }
        }
    }
}
