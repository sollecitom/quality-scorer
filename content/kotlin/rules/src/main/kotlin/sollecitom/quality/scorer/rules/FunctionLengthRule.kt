package sollecitom.quality.scorer.rules

import sollecitom.quality.scorer.domain.Finding
import sollecitom.quality.scorer.domain.QualityRule
import sollecitom.quality.scorer.domain.RuleResult
import sollecitom.quality.scorer.domain.Score
import sollecitom.quality.scorer.domain.Severity
import sollecitom.quality.scorer.model.KotlinModel

/**
 * Scores production functions by the fraction that stay within [maxLines]. A codebase with no production
 * functions is vacuously perfect. [maxLines] is injected by the assembler.
 */
class FunctionLengthRule(private val maxLines: Int = 30) : QualityRule<KotlinModel> {

    init {
        require(maxLines >= 1) { "maxLines must be >= 1 but was $maxLines" }
    }

    override suspend fun invoke(model: KotlinModel): RuleResult {
        val functions = model.productionFunctions
        if (functions.isEmpty()) return RuleResult(Score.perfect)
        val tooLong = functions.filter { it.lineCount > maxLines }
        val score = (functions.size - tooLong.size).toDouble() / functions.size
        val findings = tooLong.map { Finding("function '${it.name}' is ${it.lineCount} lines (max $maxLines)", Severity.MINOR) }
        return RuleResult(Score(score), findings)
    }
}
