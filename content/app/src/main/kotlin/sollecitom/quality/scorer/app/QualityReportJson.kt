package sollecitom.quality.scorer.app

import sollecitom.quality.scorer.domain.QualityReport

/** Renders a [QualityReport] as reward JSON (dependency-free). `reward` is the overall gradient score. */
fun QualityReport.toJson(): String {
    val rules = perRule.entries.sortedBy { it.key.value }.joinToString(",\n") { (id, result) ->
        val findings = result.findings.joinToString(", ") { finding ->
            """{"message": ${jsonString(finding.message)}, "severity": "${finding.severity}"}"""
        }
        """    {"id": "${id.value}", "score": ${result.score.value}, "findings": [$findings]}"""
    }
    return """
        |{
        |  "reward": ${overall.value},
        |  "overall": ${overall.value},
        |  "rules": [
        |$rules
        |  ]
        |}
    """.trimMargin()
}

private fun jsonString(value: String): String = buildString {
    append('"')
    for (c in value) when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
    }
    append('"')
}
