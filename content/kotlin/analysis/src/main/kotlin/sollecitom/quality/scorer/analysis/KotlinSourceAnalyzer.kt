package sollecitom.quality.scorer.analysis

import sollecitom.quality.scorer.model.KotlinSourceFile

/** Turns a single Kotlin source file's text into the [KotlinSourceFile] facts the rules judge. */
fun interface KotlinSourceAnalyzer {

    fun analyze(path: String, source: String, isTestSource: Boolean): KotlinSourceFile
}
