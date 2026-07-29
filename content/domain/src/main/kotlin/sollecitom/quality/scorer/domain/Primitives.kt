package sollecitom.quality.scorer.domain

/** A normalized quality (sub-)score in the closed unit interval [0, 1]. */
@JvmInline
value class Score(val value: Double) {

    init {
        require(value in 0.0..1.0) { "score must be in [0, 1] but was $value" }
    }

    companion object {
        val zero: Score = Score(0.0)
        val perfect: Score = Score(1.0)
    }
}

/** A non-negative relative weight assigned to a rule EXTERNALLY (never by the rule itself). */
@JvmInline
value class Weight(val value: Double) {

    init {
        require(value.isFinite() && value >= 0.0) { "weight must be finite and >= 0 but was $value" }
    }
}

/** Stable identity of a rule, used by the external scoring profile to reference and weight it. */
@JvmInline
value class RuleId(val value: String) {

    init {
        require(value.isNotBlank()) { "rule id must not be blank" }
    }

    override fun toString() = value
}
