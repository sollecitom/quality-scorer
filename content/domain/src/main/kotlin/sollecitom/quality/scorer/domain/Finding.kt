package sollecitom.quality.scorer.domain

/** Severity of a single finding. Evidence only — it does not affect the score directly. */
enum class Severity { INFO, MINOR, MAJOR, CRITICAL }

/** Where a finding occurs, when the rule can attribute it to a location in the project. */
data class SourceLocation(val path: String, val line: Int? = null, val column: Int? = null) {

    init {
        require(path.isNotBlank()) { "path must not be blank" }
        require(line == null || line >= 1) { "line must be >= 1 but was $line" }
        require(column == null || column >= 1) { "column must be >= 1 but was $column" }
    }
}

/** A single piece of evidence a rule surfaces for the dossier. Never re-scored. */
data class Finding(val message: String, val severity: Severity, val location: SourceLocation? = null) {

    init {
        require(message.isNotBlank()) { "finding message must not be blank" }
    }
}
