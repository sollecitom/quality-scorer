package sollecitom.quality.scorer.rules

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.hasSize
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.quality.scorer.model.CoverageReport
import sollecitom.quality.scorer.model.KotlinModel

@TestInstance(PER_CLASS)
class CoverageBandRuleTests {

    private val rule = CoverageBandRule(low = 0.60, high = 0.85)

    private fun model(lineRate: Double?) = KotlinModel(
        sourceFiles = emptyList(),
        coverage = lineRate?.let { CoverageReport(lineRate = it, branchRate = it) },
    )

    @Test
    fun `missing coverage scores zero with a finding`() = runTest {
        val result = rule(model(null))
        assertThat(result.score.value).isEqualTo(0.0)
        assertThat(result.findings).hasSize(1)
    }

    @Test
    fun `coverage at or below low scores zero`() = runTest {
        assertThat(rule(model(0.50)).score.value).isEqualTo(0.0)
        assertThat(rule(model(0.60)).score.value).isEqualTo(0.0)
    }

    @Test
    fun `coverage at or above high scores one with no findings`() = runTest {
        val result = rule(model(0.90))
        assertThat(result.score.value).isEqualTo(1.0)
        assertThat(result.findings).isEmpty()
    }

    @Test
    fun `coverage between low and high interpolates linearly`() = runTest {
        // (0.725 - 0.60) / (0.85 - 0.60) = 0.5
        assertThat(rule(model(0.725)).score.value).isCloseTo(0.5, 1e-9)
    }
}
