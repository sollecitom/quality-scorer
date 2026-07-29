package sollecitom.quality.scorer.domain

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class WeightedRuleSetScorerTests {

    private object Model : AssessmentModel

    private fun constant(id: String, weight: Double, score: Double) =
        WeightedRule<AssessmentModel>(RuleId(id), Weight(weight), QualityRule { RuleResult(Score(score)) })

    @Test
    fun `overall is the weighted mean of the rule scores`() = runTest {
        val profile = ScoringProfile(listOf(constant("a", 1.0, 1.0), constant("b", 3.0, 0.0)))

        val report = WeightedRuleSetScorer(profile).invoke(Model)

        assertEquals(0.25, report.overall.value, 1e-9) // (1*1 + 3*0) / 4
    }

    @Test
    fun `report carries every rule result keyed by its id`() = runTest {
        val profile = ScoringProfile(listOf(constant("a", 1.0, 0.4), constant("b", 1.0, 0.6)))

        val report = WeightedRuleSetScorer(profile).invoke(Model)

        assertEquals(setOf(RuleId("a"), RuleId("b")), report.perRule.keys)
        assertEquals(0.5, report.overall.value, 1e-9)
    }
}
