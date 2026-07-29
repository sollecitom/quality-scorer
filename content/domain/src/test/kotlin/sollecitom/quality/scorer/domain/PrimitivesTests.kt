package sollecitom.quality.scorer.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows

@TestInstance(PER_CLASS)
class PrimitivesTests {

    @Test
    fun `score accepts values within the unit interval`() {
        assertEquals(0.0, Score(0.0).value)
        assertEquals(0.5, Score(0.5).value)
        assertEquals(1.0, Score(1.0).value)
    }

    @Test
    fun `score rejects values outside the unit interval`() {
        assertThrows<IllegalArgumentException> { Score(-0.001) }
        assertThrows<IllegalArgumentException> { Score(1.001) }
    }

    @Test
    fun `weight rejects negative or non-finite values`() {
        assertThrows<IllegalArgumentException> { Weight(-1.0) }
        assertThrows<IllegalArgumentException> { Weight(Double.NaN) }
    }

    @Test
    fun `rule id rejects blanks`() {
        assertThrows<IllegalArgumentException> { RuleId("   ") }
    }
}
