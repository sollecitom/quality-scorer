package sollecitom.quality.scorer.analysis

import sollecitom.quality.scorer.model.CoverageReport
import sollecitom.quality.scorer.model.InvalidInputException
import sollecitom.quality.scorer.model.KotlinModel
import java.io.File

/** Walks a project directory, analyzes every `.kt` file, and assembles a [KotlinModel]. */
class KotlinProjectLoader(private val analyzer: KotlinSourceAnalyzer = HeuristicKotlinAnalyzer()) {

    fun load(projectRoot: File, coverage: CoverageReport? = null): KotlinModel {
        val sourceFiles = projectRoot.walkTopDown()
            .onEnter { it == projectRoot || it.name !in buildDirectoryNames }
            .filter { it.isFile && it.extension == "kt" }
            .map { file ->
                val relative = file.relativeTo(projectRoot).path
                analyzer.analyze(path = relative, source = file.readText(), isTestSource = isTestPath(relative))
            }
            .toList()
        if (sourceFiles.isEmpty()) throw InvalidInputException("no Kotlin files in project: $projectRoot")
        return KotlinModel(sourceFiles = sourceFiles, coverage = coverage)
    }

    private fun isTestPath(relative: String): Boolean = testSourceSetPath.containsMatchIn(relative.replace(File.separatorChar, '/'))

    private companion object {
        val buildDirectoryNames = setOf("build", ".gradle", ".kotlin")
        val testSourceSetPath = Regex("(^|/)src/[^/]*[Tt]est[^/]*/")
    }
}
