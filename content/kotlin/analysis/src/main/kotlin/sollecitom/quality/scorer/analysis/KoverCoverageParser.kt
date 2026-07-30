package sollecitom.quality.scorer.analysis

import org.w3c.dom.Element
import sollecitom.quality.scorer.model.CoverageReport
import java.io.ByteArrayInputStream
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parses a kover / JaCoCo-format XML report into a [CoverageReport]. The aggregate LINE and BRANCH totals are
 * the `<counter>` elements that are direct children of the root `<report>`. Pure JDK XML — no dependency, and
 * external DTD loading is disabled so it stays offline (JaCoCo reports carry a DOCTYPE).
 */
object KoverCoverageParser {

    fun parseFile(file: File): CoverageReport? = if (file.isFile) parse(file.readText()) else null

    fun parse(xml: String): CoverageReport? {
        val root = try {
            val factory = DocumentBuilderFactory.newInstance()
            runCatching { factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
            runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
            factory.isValidating = false
            factory.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray())).documentElement
        } catch (_: Exception) {
            return null
        } ?: return null

        val counters = (0 until root.childNodes.length)
            .mapNotNull { root.childNodes.item(it) as? Element }
            .filter { it.tagName == "counter" }
        val lineRate = counters.firstOrNull { it.getAttribute("type") == "LINE" }?.let(::rate) ?: return null
        val branchRate = counters.firstOrNull { it.getAttribute("type") == "BRANCH" }?.let(::rate) ?: 1.0
        return CoverageReport(lineRate = lineRate, branchRate = branchRate)
    }

    private fun rate(counter: Element): Double? {
        val missed = counter.getAttribute("missed").toIntOrNull() ?: return null
        val covered = counter.getAttribute("covered").toIntOrNull() ?: return null
        val total = missed + covered
        return if (total == 0) 1.0 else covered.toDouble() / total
    }
}
