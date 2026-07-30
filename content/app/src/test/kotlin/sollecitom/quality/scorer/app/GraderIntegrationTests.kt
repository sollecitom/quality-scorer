package sollecitom.quality.scorer.app

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThan
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.quality.scorer.domain.RuleId
import java.io.File

/**
 * End-to-end grade of two t-bench-like completed Kotlin projects (a `LoyaltyPointsLedger` implemented cleanly
 * vs. messily). This is the harness we iterate the rules against: tweak a rule/threshold/weight and watch the
 * clean vs messy scores move.
 */
@TestInstance(PER_CLASS)
class GraderIntegrationTests {

    private val grader = Grader()

    private fun fixture(name: String): File = File(javaClass.getResource("/fixtures/$name")!!.toURI())
    private fun coverage(name: String): File = File(fixture(name), "coverage.xml")

    @Test
    fun `a clean project scores much higher than a messy one`() = runTest {
        val clean = grader.grade(fixture("clean"), coverage("clean"))
        val messy = grader.grade(fixture("messy"), coverage("messy"))
        assertThat(clean.overall.value).isGreaterThan(messy.overall.value)
        assertThat(clean.overall.value - messy.overall.value).isGreaterThan(0.5)
    }

    @Test
    fun `the clean project earns full marks on each rule`() = runTest {
        val report = grader.grade(fixture("clean"), coverage("clean"))
        assertThat(report.scoreOf("function-length")).isEqualTo(1.0)
        assertThat(report.scoreOf("coverage")).isEqualTo(1.0)
        assertThat(report.scoreOf("assertion-density")).isEqualTo(1.0)
        assertThat(report.scoreOf("public-api-kdoc")).isEqualTo(1.0)
        assertThat(report.overall.value).isGreaterThanOrEqualTo(0.85)
    }

    @Test
    fun `the messy project is penalised on docs, length, coverage and vacuous tests`() = runTest {
        val report = grader.grade(fixture("messy"), coverage("messy"))
        assertThat(report.scoreOf("public-api-kdoc")).isEqualTo(0.0)
        assertThat(report.scoreOf("function-length")).isEqualTo(0.0)
        assertThat(report.scoreOf("coverage")).isEqualTo(0.0)
        assertThat(report.scoreOf("assertion-density")).isLessThan(1.0)
    }

    @Test
    fun `renders reward json`() = runTest {
        val json = grader.grade(fixture("clean"), coverage("clean")).toJson()
        assertThat(json).contains("\"reward\"")
        assertThat(json).contains("public-api-kdoc")
    }

    private fun sollecitom.quality.scorer.domain.QualityReport.scoreOf(id: String) =
        perRule.getValue(RuleId(id)).score.value
}
