package loyalty

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class LoyaltyPointsLedgerTest {

    @Test
    fun `accrues`() {
        val ledger = LoyaltyPointsLedger()
        assertThat(ledger.process("ada", "accrue", 10)).isEqualTo(10)
    }

    @Test
    fun `runs accrue without checking anything`() {
        val ledger = LoyaltyPointsLedger()
        ledger.process("ada", "accrue", 10)
    }

    @Test
    fun `also just runs`() {
        LoyaltyPointsLedger().process("x", "balance", 0)
    }
}
