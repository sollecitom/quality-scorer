package sollecitom.quality.scorer.app

import sollecitom.quality.scorer.analysis.KoverCoverageParser
import sollecitom.quality.scorer.analysis.KotlinProjectLoader
import sollecitom.quality.scorer.domain.QualityReport
import sollecitom.quality.scorer.domain.ScoringProfile
import sollecitom.quality.scorer.domain.WeightedRuleSetScorer
import sollecitom.quality.scorer.model.KotlinModel
import java.io.File

/** Loads a Kotlin project (+ optional coverage report) and scores it against a [ScoringProfile]. */
class Grader(
    private val loader: KotlinProjectLoader = KotlinProjectLoader(),
    profile: ScoringProfile<KotlinModel> = defaultKotlinScoringProfile(),
) {
    private val scorer = WeightedRuleSetScorer(profile)

    suspend fun grade(projectRoot: File, coverageReport: File? = null): QualityReport {
        val coverage = coverageReport?.let { KoverCoverageParser.parseFile(it) }
        val model = loader.load(projectRoot, coverage)
        return scorer(model)
    }
}
