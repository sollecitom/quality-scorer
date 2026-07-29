package sollecitom.quality.scorer.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** A rule registered with its EXTERNAL identity and weight. The rule itself stays weight/config-agnostic. */
data class WeightedRule<M : AssessmentModel>(val id: RuleId, val weight: Weight, val rule: QualityRule<M>)

/** The external, per-task tuning surface: which rules run and their relative weights. */
data class ScoringProfile<M : AssessmentModel>(val rules: List<WeightedRule<M>>) {

    init {
        require(rules.isNotEmpty()) { "a scoring profile needs at least one rule" }
        require(rules.map { it.id }.toSet().size == rules.size) { "rule ids must be unique within a profile" }
        require(rules.sumOf { it.weight.value } > 0.0) { "the total weight of a profile must be positive" }
    }
}

/** The aggregate outcome over a profile: the overall weighted score and the per-rule breakdown. */
data class QualityReport(val overall: Score, val perRule: Map<RuleId, RuleResult>)

/** Scores a single model against a rule set. */
fun interface QualityScorer<in M : AssessmentModel> {

    suspend operator fun invoke(model: M): QualityReport
}

/**
 * Default scorer: runs every rule in the profile CONCURRENTLY over the model, then aggregates into a
 * weighted-mean overall score — `Σ(weightᵢ · scoreᵢ) / Σweightᵢ`.
 */
class WeightedRuleSetScorer<M : AssessmentModel>(private val profile: ScoringProfile<M>) : QualityScorer<M> {

    override suspend fun invoke(model: M): QualityReport = coroutineScope {
        val evaluated = profile.rules.map { weighted -> async { weighted to weighted.rule(model) } }.awaitAll()
        val totalWeight = evaluated.sumOf { (weighted, _) -> weighted.weight.value }
        val weightedScore = evaluated.sumOf { (weighted, result) -> weighted.weight.value * result.score.value }
        QualityReport(
            overall = Score(weightedScore / totalWeight),
            perRule = evaluated.associate { (weighted, result) -> weighted.id to result },
        )
    }
}
