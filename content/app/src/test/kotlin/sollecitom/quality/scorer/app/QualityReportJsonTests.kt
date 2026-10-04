package sollecitom.quality.scorer.app

import assertk.assertThat
import assertk.assertions.contains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.quality.scorer.domain.Finding
import sollecitom.quality.scorer.domain.QualityReport
import sollecitom.quality.scorer.domain.RuleId
import sollecitom.quality.scorer.domain.RuleResult
import sollecitom.quality.scorer.domain.Score
import sollecitom.quality.scorer.domain.Severity

@TestInstance(PER_CLASS)
class QualityReportJsonTests {

    @Test
    fun `escapes control characters in finding messages`() {
        val report = QualityReport(
            overall = Score(1.0),
            perRule = mapOf(RuleId("rule") to RuleResult(Score(1.0), listOf(Finding("a\u0001b\u001fc", Severity.INFO)))),
        )

        assertThat(report.toJson()).contains(""""message": "a\u0001b\u001fc"""")
    }
}
