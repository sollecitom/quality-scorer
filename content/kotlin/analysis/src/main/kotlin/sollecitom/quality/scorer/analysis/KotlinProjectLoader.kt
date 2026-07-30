package sollecitom.quality.scorer.analysis

import sollecitom.quality.scorer.model.CoverageReport
import sollecitom.quality.scorer.model.KotlinModel
import java.io.File

/** Walks a project directory, analyzes every `.kt` file, and assembles a [KotlinModel]. */
class KotlinProjectLoader(private val analyzer: KotlinSourceAnalyzer = HeuristicKotlinAnalyzer()) {

    fun load(projectRoot: File, coverage: CoverageReport? = null): KotlinModel {
        val sourceFiles = projectRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { file ->
                val relative = file.relativeTo(projectRoot).path
                analyzer.analyze(path = relative, source = file.readText(), isTestSource = isTestPath(relative))
            }
            .toList()
        return KotlinModel(sourceFiles = sourceFiles, coverage = coverage)
    }

    private fun isTestPath(relative: String): Boolean {
        val path = relative.replace(File.separatorChar, '/')
        return "src/test/" in path || "/test/" in path || path.startsWith("test/")
    }
}
