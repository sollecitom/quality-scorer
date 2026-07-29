package sollecitom.quality.scorer.rules

import sollecitom.quality.scorer.domain.Finding
import sollecitom.quality.scorer.domain.QualityRule
import sollecitom.quality.scorer.domain.RuleResult
import sollecitom.quality.scorer.domain.Score
import sollecitom.quality.scorer.domain.Severity
import sollecitom.quality.scorer.model.KotlinModel

/**
 * Scores the public API (production public functions and types) by the fraction carrying KDoc. This is a
 * presence/coverage check only — doc CLARITY is a later LLM-judge rule. A project with no public API is
 * vacuously perfect.
 */
class PublicApiKDocRule : QualityRule<KotlinModel> {

    override suspend fun invoke(model: KotlinModel): RuleResult {
        val documentable = model.publicApiFunctions.map { it.name to it.hasKdoc } +
            model.publicApiTypes.map { it.name to it.hasKdoc }
        if (documentable.isEmpty()) return RuleResult(Score.perfect)
        val undocumented = documentable.filterNot { (_, hasKdoc) -> hasKdoc }
        val score = (documentable.size - undocumented.size).toDouble() / documentable.size
        val findings = undocumented.map { (name, _) -> Finding("public declaration '$name' has no KDoc", Severity.MINOR) }
        return RuleResult(Score(score), findings)
    }
}
