package sollecitom.quality.scorer.rules

import sollecitom.quality.scorer.domain.Finding
import sollecitom.quality.scorer.domain.QualityRule
import sollecitom.quality.scorer.domain.RuleResult
import sollecitom.quality.scorer.domain.Score
import sollecitom.quality.scorer.domain.Severity
import sollecitom.quality.scorer.model.KotlinModel

/**
 * Grades line coverage on a piecewise-linear band: at or below [low] scores 0, at or above [high] scores 1,
 * and it interpolates linearly in between. [low] and [high] are injected by the assembler (config is external
 * to the rule). A missing coverage report scores 0 with a finding.
 */
class CoverageBandRule(private val low: Double = 0.60, private val high: Double = 0.85) : QualityRule<KotlinModel> {

    init {
        require(low in 0.0..1.0 && high in 0.0..1.0 && low < high) { "require 0 <= low < high <= 1, got low=$low high=$high" }
    }

    override suspend fun invoke(model: KotlinModel): RuleResult {
        val coverage = model.coverage
            ?: return RuleResult(Score.zero, listOf(Finding("no coverage report available", Severity.MAJOR)))
        val rate = coverage.lineRate
        val score = when {
            rate <= low -> 0.0
            rate >= high -> 1.0
            else -> (rate - low) / (high - low)
        }
        val findings = if (score < 1.0) {
            listOf(Finding("line coverage ${percent(rate)} is below the target ${percent(high)}", Severity.MINOR))
        } else {
            emptyList()
        }
        return RuleResult(Score(score), findings)
    }

    private fun percent(rate: Double) = "${Math.round(rate * 100)}%"
}
