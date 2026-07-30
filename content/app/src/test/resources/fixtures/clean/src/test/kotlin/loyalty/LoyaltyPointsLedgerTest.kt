package loyalty

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class LoyaltyPointsLedgerTest {

    @Test
    fun `accrues points onto the balance`() {
        val ledger = LoyaltyPointsLedger()
        assertThat(ledger.accrue("ada", 10)).isEqualTo(10)
    }

    @Test
    fun `redeems points from the balance`() {
        val ledger = LoyaltyPointsLedger()
        ledger.accrue("ada", 10)
        assertThat(ledger.redeem("ada", 4)).isEqualTo(6)
    }

    @Test
    fun `reports the current balance`() {
        val ledger = LoyaltyPointsLedger()
        ledger.accrue("ada", 7)
        assertThat(ledger.balanceOf("ada")).isEqualTo(7)
    }
}
