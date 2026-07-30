package loyalty

/** Immutable snapshot of a member's loyalty balance. */
data class Balance(val member: String, val points: Int)

/** Accrues and redeems loyalty points for members, keeping a running balance per member. */
class LoyaltyPointsLedger {

    private val balances = mutableMapOf<String, Int>()

    /** Adds [points] to [member]'s balance and returns the new total. */
    fun accrue(member: String, points: Int): Int {
        require(points >= 0) { "points must be non-negative" }
        val total = balances.getOrDefault(member, 0) + points
        balances[member] = total
        return total
    }

    /** Redeems [points] from [member], failing if the balance is insufficient. */
    fun redeem(member: String, points: Int): Int {
        val current = balances.getOrDefault(member, 0)
        require(points in 0..current) { "cannot redeem $points from $current" }
        val total = current - points
        balances[member] = total
        return total
    }

    /** The current balance for [member], or zero if the member is unknown. */
    fun balanceOf(member: String): Int = balances.getOrDefault(member, 0)
}
