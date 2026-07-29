package sollecitom.quality.scorer.rules

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.hasSize
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.quality.scorer.model.KotlinFunction
import sollecitom.quality.scorer.model.KotlinModel
import sollecitom.quality.scorer.model.KotlinSourceFile
import sollecitom.quality.scorer.model.KotlinTypeDeclaration

@TestInstance(PER_CLASS)
class KotlinRulesTests {

    @Test
    fun `function length scores the fraction within the limit`() = runTest {
        val model = KotlinModel(
            listOf(
                KotlinSourceFile(
                    "Main.kt",
                    functions = listOf(
                        KotlinFunction("a", lineCount = 10),
                        KotlinFunction("b", lineCount = 20),
                        KotlinFunction("c", lineCount = 25),
                        KotlinFunction("tooLong", lineCount = 60),
                    ),
                ),
            ),
        )
        val result = FunctionLengthRule(maxLines = 30)(model)
        assertThat(result.score.value).isCloseTo(0.75, 1e-9)
        assertThat(result.findings).hasSize(1)
    }

    @Test
    fun `function length is vacuously perfect with no production functions`() = runTest {
        val result = FunctionLengthRule()(KotlinModel(emptyList()))
        assertThat(result.score.value).isEqualTo(1.0)
    }

    @Test
    fun `assertion density scores the fraction of tests that assert`() = runTest {
        val model = KotlinModel(
            listOf(
                KotlinSourceFile(
                    "MainTest.kt",
                    isTestSource = true,
                    functions = listOf(
                        KotlinFunction("t1", lineCount = 5, isTest = true, assertionCount = 2),
                        KotlinFunction("t2", lineCount = 5, isTest = true, assertionCount = 1),
                        KotlinFunction("vacuous", lineCount = 5, isTest = true, assertionCount = 0),
                    ),
                ),
            ),
        )
        val result = AssertionDensityRule()(model)
        assertThat(result.score.value).isCloseTo(2.0 / 3.0, 1e-9)
        assertThat(result.findings).hasSize(1)
    }

    @Test
    fun `assertion density scores zero when there are no tests`() = runTest {
        val result = AssertionDensityRule()(KotlinModel(emptyList()))
        assertThat(result.score.value).isEqualTo(0.0)
        assertThat(result.findings).hasSize(1)
    }

    @Test
    fun `public api kdoc scores the documented fraction`() = runTest {
        val model = KotlinModel(
            listOf(
                KotlinSourceFile(
                    "Api.kt",
                    functions = listOf(
                        KotlinFunction("documented", lineCount = 3, isPublic = true, hasKdoc = true),
                        KotlinFunction("undocumented", lineCount = 3, isPublic = true, hasKdoc = false),
                        KotlinFunction("privateOne", lineCount = 3, isPublic = false, hasKdoc = false),
                    ),
                    types = listOf(KotlinTypeDeclaration("PublicType", isPublic = true, hasKdoc = true)),
                ),
            ),
        )
        // documentable = documented(true) + undocumented(false) + PublicType(true) -> 2 of 3 documented
        val result = PublicApiKDocRule()(model)
        assertThat(result.score.value).isCloseTo(2.0 / 3.0, 1e-9)
        assertThat(result.findings).hasSize(1)
    }
}
