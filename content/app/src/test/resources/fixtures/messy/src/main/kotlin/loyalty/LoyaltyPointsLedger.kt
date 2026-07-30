package loyalty

class LoyaltyPointsLedger {

    private val balances = HashMap<String, Int>()
    private val history = ArrayList<String>()

    fun process(member: String, op: String, points: Int): Int {
        var total = balances.getOrDefault(member, 0)
        var result = total
        if (op == "accrue") {
            if (points < 0) {
                throw IllegalArgumentException("bad points")
            }
            total = total + points
            balances[member] = total
            history.add("accrue $member $points")
            result = total
        } else if (op == "redeem") {
            if (points < 0) {
                throw IllegalArgumentException("bad points")
            }
            if (points > total) {
                throw IllegalArgumentException("insufficient")
            }
            total = total - points
            balances[member] = total
            history.add("redeem $member $points")
            result = total
        } else if (op == "balance") {
            result = balances.getOrDefault(member, 0)
            history.add("balance $member")
        } else if (op == "reset") {
            balances[member] = 0
            history.add("reset $member")
            result = 0
        } else {
            throw IllegalArgumentException("unknown op $op")
        }
        return result
    }
}
