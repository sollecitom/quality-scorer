package sollecitom.quality.scorer.app

import sollecitom.quality.scorer.domain.RuleId
import sollecitom.quality.scorer.domain.ScoringProfile
import sollecitom.quality.scorer.domain.Weight
import sollecitom.quality.scorer.domain.WeightedRule
import sollecitom.quality.scorer.model.KotlinModel
import sollecitom.quality.scorer.rules.AssertionDensityRule
import sollecitom.quality.scorer.rules.CoverageBandRule
import sollecitom.quality.scorer.rules.FunctionLengthRule
import sollecitom.quality.scorer.rules.PublicApiKDocRule

/**
 * The default Kotlin MVP profile. Weights (external to the rules) split roughly structural 0.50 / tests 0.35 /
 * docs 0.15 — the per-task tuning surface, meant to be iterated on.
 */
fun defaultKotlinScoringProfile(): ScoringProfile<KotlinModel> = ScoringProfile(
    listOf(
        WeightedRule(RuleId("function-length"), Weight(0.50), FunctionLengthRule()),
        WeightedRule(RuleId("coverage"), Weight(0.175), CoverageBandRule()),
        WeightedRule(RuleId("assertion-density"), Weight(0.175), AssertionDensityRule()),
        WeightedRule(RuleId("public-api-kdoc"), Weight(0.15), PublicApiKDocRule()),
    ),
)
