package sollecitom.quality.scorer.rules

import sollecitom.quality.scorer.domain.Finding
import sollecitom.quality.scorer.domain.QualityRule
import sollecitom.quality.scorer.domain.RuleResult
import sollecitom.quality.scorer.domain.Score
import sollecitom.quality.scorer.domain.Severity
import sollecitom.quality.scorer.model.KotlinModel

/**
 * Scores test functions by the fraction that make at least [minAssertions] assertion call(s) — a proxy for
 * "the tests actually check something" that catches vacuous tests. A project with no tests scores 0 with a
 * finding (absence of tests is itself a quality failure for this axis). [minAssertions] is injected.
 */
class AssertionDensityRule(private val minAssertions: Int = 1) : QualityRule<KotlinModel> {

    init {
        require(minAssertions >= 1) { "minAssertions must be >= 1 but was $minAssertions" }
    }

    override suspend fun invoke(model: KotlinModel): RuleResult {
        val tests = model.testFunctions
        if (tests.isEmpty()) return RuleResult(Score.zero, listOf(Finding("no test functions found", Severity.MAJOR)))
        val vacuous = tests.filter { it.assertionCount < minAssertions }
        val score = (tests.size - vacuous.size).toDouble() / tests.size
        val findings = vacuous.map { Finding("test '${it.name}' makes no assertions", Severity.MAJOR) }
        return RuleResult(Score(score), findings)
    }
}
