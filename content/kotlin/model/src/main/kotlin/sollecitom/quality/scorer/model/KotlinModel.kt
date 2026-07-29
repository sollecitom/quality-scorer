package sollecitom.quality.scorer.model

import sollecitom.quality.scorer.domain.AssessmentModel

/** Line/branch coverage rates in [0, 1], parsed from a coverage report (e.g. a kover XML report). */
data class CoverageReport(val lineRate: Double, val branchRate: Double) {

    init {
        require(lineRate in 0.0..1.0) { "lineRate must be in [0, 1] but was $lineRate" }
        require(branchRate in 0.0..1.0) { "branchRate must be in [0, 1] but was $branchRate" }
    }
}

/**
 * A function/method declaration with the facts the MVP rules judge. Produced by an analyzer (a PSI-backed
 * one lands in a later increment); the rules never touch PSI or any analysis tool directly.
 */
data class KotlinFunction(
    val name: String,
    val lineCount: Int,
    val cyclomaticComplexity: Int = 1,
    val isPublic: Boolean = true,
    val hasKdoc: Boolean = false,
    val isTest: Boolean = false,
    val assertionCount: Int = 0,
) {
    init {
        require(name.isNotBlank()) { "function name must not be blank" }
        require(lineCount >= 0) { "lineCount must be >= 0 but was $lineCount" }
        require(cyclomaticComplexity >= 1) { "cyclomaticComplexity must be >= 1 but was $cyclomaticComplexity" }
        require(assertionCount >= 0) { "assertionCount must be >= 0 but was $assertionCount" }
    }
}

/** A type (class/interface/object) declaration. */
data class KotlinTypeDeclaration(val name: String, val isPublic: Boolean = true, val hasKdoc: Boolean = false) {

    init {
        require(name.isNotBlank()) { "type name must not be blank" }
    }
}

/** One Kotlin source file's declarations, and whether it belongs to a test source set. */
data class KotlinSourceFile(
    val path: String,
    val functions: List<KotlinFunction> = emptyList(),
    val types: List<KotlinTypeDeclaration> = emptyList(),
    val isTestSource: Boolean = false,
) {
    init {
        require(path.isNotBlank()) { "path must not be blank" }
    }
}

/** The Kotlin view of a project: its source files plus an optional coverage report. */
data class KotlinModel(
    val sourceFiles: List<KotlinSourceFile>,
    val coverage: CoverageReport? = null,
) : AssessmentModel {

    val allFunctions: List<KotlinFunction> get() = sourceFiles.flatMap { it.functions }

    /** A test is any function marked `@Test` (the analyzer sets [KotlinFunction.isTest]). */
    val testFunctions: List<KotlinFunction> get() = allFunctions.filter { it.isTest }

    /** Production functions are the non-test functions. */
    val productionFunctions: List<KotlinFunction> get() = allFunctions.filterNot { it.isTest }

    val publicApiFunctions: List<KotlinFunction>
        get() = sourceFiles.filterNot { it.isTestSource }.flatMap { it.functions }.filter { it.isPublic && !it.isTest }

    val publicApiTypes: List<KotlinTypeDeclaration>
        get() = sourceFiles.filterNot { it.isTestSource }.flatMap { it.types }.filter { it.isPublic }
}
