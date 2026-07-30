package sollecitom.quality.scorer.analysis

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class HeuristicKotlinAnalyzerTests {

    private val analyzer = HeuristicKotlinAnalyzer()

    @Test
    fun `extracts visibility and kdoc, ignoring keywords inside strings`() {
        val source = """
            package x

            /** Adds two numbers. */
            fun add(a: Int, b: Int): Int {
                return a + b
            }

            private fun helper() {
                println("noise with fun class { } inside a string")
            }
        """.trimIndent()
        val file = analyzer.analyze("src/main/kotlin/X.kt", source, isTestSource = false)

        assertThat(file.functions.map { it.name }).containsExactlyInAnyOrder("add", "helper")
        val add = file.functions.single { it.name == "add" }
        assertThat(add.isPublic).isTrue()
        assertThat(add.hasKdoc).isTrue()
        assertThat(add.isTest).isFalse()
        val helper = file.functions.single { it.name == "helper" }
        assertThat(helper.isPublic).isFalse()
        assertThat(helper.hasKdoc).isFalse()
    }

    @Test
    fun `detects tests (same-line annotation) and counts assertions`() {
        val source = """
            import org.junit.jupiter.api.Test
            class FooTest {
                @Test fun `does a thing`() {
                    val x = compute()
                    assertThat(x).isEqualTo(2)
                    assertThat(x).isGreaterThan(0)
                }

                @Test
                fun vacuous() {
                    compute()
                }
            }
        """.trimIndent()
        val file = analyzer.analyze("src/test/kotlin/FooTest.kt", source, isTestSource = true)

        val good = file.functions.single { it.name == "does a thing" }
        assertThat(good.isTest).isTrue()
        assertThat(good.assertionCount).isEqualTo(2)
        val vacuous = file.functions.single { it.name == "vacuous" }
        assertThat(vacuous.isTest).isTrue()
        assertThat(vacuous.assertionCount).isEqualTo(0)
    }

    @Test
    fun `measures block function length across lines`() {
        val source = buildString {
            appendLine("fun long() {")
            repeat(40) { appendLine("    println($it)") }
            append("}")
        }
        assertThat(analyzer.analyze("X.kt", source, false).functions.single().lineCount).isEqualTo(42)
    }

    @Test
    fun `extracts public and private types with kdoc`() {
        val source = """
            /** A widget. */
            class Widget

            private class Hidden
        """.trimIndent()
        val file = analyzer.analyze("X.kt", source, false)

        val widget = file.types.single { it.name == "Widget" }
        assertThat(widget.isPublic).isTrue()
        assertThat(widget.hasKdoc).isTrue()
        assertThat(file.types.single { it.name == "Hidden" }.isPublic).isFalse()
    }
}
