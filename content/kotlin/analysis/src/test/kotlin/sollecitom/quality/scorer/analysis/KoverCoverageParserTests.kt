package sollecitom.quality.scorer.analysis

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class KoverCoverageParserTests {

    @Test
    fun `parses line and branch rates from a jacoco-format report with a doctype`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
            <report name="app">
              <package name="x"><class name="x/A"/></package>
              <counter type="INSTRUCTION" missed="10" covered="90"/>
              <counter type="BRANCH" missed="2" covered="6"/>
              <counter type="LINE" missed="1" covered="9"/>
              <counter type="METHOD" missed="0" covered="5"/>
            </report>
        """.trimIndent()
        val report = KoverCoverageParser.parse(xml)!!
        assertThat(report.lineRate).isCloseTo(0.9, 1e-9)     // 9 / (1 + 9)
        assertThat(report.branchRate).isCloseTo(0.75, 1e-9)  // 6 / (2 + 6)
    }

    @Test
    fun `defaults branch rate to one when there is no branch counter`() {
        val report = KoverCoverageParser.parse("""<report><counter type="LINE" missed="0" covered="4"/></report>""")!!
        assertThat(report.lineRate).isEqualTo(1.0)
        assertThat(report.branchRate).isEqualTo(1.0)
    }

    @Test
    fun `returns null when there is no line counter`() {
        assertThat(KoverCoverageParser.parse("""<report><counter type="METHOD" missed="1" covered="1"/></report>""")).isNull()
    }

    @Test
    fun `returns null on malformed xml`() {
        assertThat(KoverCoverageParser.parse("not xml at all")).isNull()
    }
}
