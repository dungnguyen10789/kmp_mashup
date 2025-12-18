import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Convention Plugin: Architecture Compliance Check
 *
 * This plugin defines a custom Gradle task 'archCheck' that enforces Clean Architecture rules
 * by statically analyzing source code imports. It ensures that the dependency direction
 * between modules is respected (e.g., Domain must not depend on Data or Presentation).
 */
class ArchCheckConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.register("archCheck") {
                group = "verification"
                description = "Checks Clean Architecture import rules across shared modules"

                // This task reads files from disk; keep it simple by opting out of configuration cache.
                notCompatibleWithConfigurationCache("Uses file-system scanning")

                doLast {
                    // Define module source roots. We scan 'src' to include commonMain, androidMain, etc.
                    val mobileSharedDomainRoot = rootProject.file("mobile/shared/domain/src")
                    val mobileSharedDataRoot = rootProject.file("mobile/shared/data/src")
                    val mobileSharePresentationRoot = rootProject.file("mobile/shared/presentation/src")

                    // The base package prefix for our project modules.
                    val basePackage = "vn.dna.kmp_mashup"

                    // --- RULE 1: DOMAIN LAYER ---
                    // The Domain layer is the center of the architecture.
                    // It MUST NOT depend on Data, Presentation, DI frameworks, or platform-specific Android APIs.
                    assertNoImports(
                        moduleName = "$basePackage.domain",
                        roots = listOf(mobileSharedDomainRoot),
                        forbidden = listOf(
                            "$basePackage.data",
                            "$basePackage.presentation",
                            "$basePackage.di",
                            "android.",
                            "androidx."
                        ),
                    )

                    // --- RULE 2: DATA LAYER ---
                    // The Data layer implements interfaces defined in Domain.
                    // It should not know about the UI (Presentation).
                    assertNoImports(
                        moduleName = "$basePackage.data",
                        roots = listOf(mobileSharedDataRoot),
                        forbidden = listOf(
                            "$basePackage.presentation",
                            // Data layer should usually expose modules to DI, but ideally shouldn't depend on DI *inside* logic classes.
                            // Adjust this rule if you use Koin injection directly inside Repositories.
                            "$basePackage.di", 
                        ),
                    )

                    // --- RULE 3: PRESENTATION LAYER ---
                    // The Presentation layer (UI) uses the Domain layer.
                    // It MUST NOT access the Data layer directly (bypass Domain).
                    // This enforces the rule: UI -> Domain -> Data.
                    assertNoImports(
                        moduleName = "$basePackage.presentation",
                        roots = listOf(mobileSharePresentationRoot),
                        forbidden = listOf(
                            "$basePackage.data",
                             // Presentation logic (ViewModels) should be platform-agnostic if possible,
                             // but it often needs DI injection.
                            "$basePackage.di",
                        ),
                    )
                }
            }
        }
    }

    private fun readKotlinFiles(dir: File): List<File> =
        if (!dir.exists()) emptyList()
        else dir.walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }.toList()

    private fun assertNoImports(moduleName: String, roots: List<File>, forbidden: List<String>) {
        val files = roots.flatMap { readKotlinFiles(it) }
        val violations = mutableListOf<String>()

        files.forEach { f ->
            val text = f.readText()
            forbidden.forEach { pattern ->
                // Simple string check for illegal imports.
                if (text.contains("import $pattern")) {
                    violations.add("$moduleName: ${f.name} imports forbidden package '$pattern'")
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw org.gradle.api.GradleException("Architecture violations found:\n" + violations.joinToString("\n"))
        }
    }
}
