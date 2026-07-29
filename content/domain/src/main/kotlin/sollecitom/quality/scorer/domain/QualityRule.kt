package sollecitom.quality.scorer.domain

/**
 * A typed VIEW of the software project under assessment that a rule consumes.
 *
 * NOT sealed on purpose: each language module contributes its own model (e.g. a `KotlinModel` carrying PSI
 * and resolved types) from its own Gradle module, and the scorer dispatches each rule to the model type it
 * declares. [ProjectModel] is the language-agnostic view every project has.
 */
interface AssessmentModel

/** The language-agnostic view (files, coverage report, test results, VCS, ...) available for any project. */
interface ProjectModel : AssessmentModel

/** The graded outcome of one rule: a normalized score plus evidence. No weight — weighting is external. */
data class RuleResult(val score: Score, val findings: List<Finding> = emptyList())

/**
 * A single quality rule. Pure and graded: it maps a model to a [RuleResult] and knows nothing about its own
 * weight, where its configuration came from, or the other rules in play. Suspending, so a rule can do IO or
 * call out (v2 LLM judges) and so a rule set can be run concurrently with per-rule timeouts.
 */
fun interface QualityRule<in M : AssessmentModel> {

    suspend operator fun invoke(model: M): RuleResult
}
