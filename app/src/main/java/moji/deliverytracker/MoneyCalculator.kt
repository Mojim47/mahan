package moji.deliverytracker

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyCalculator {
    fun commissionAmount(total: Int, commissionPercent: Float): Int {
        if (total <= 0 || commissionPercent <= 0f) return 0
        val totalBD = BigDecimal(total)
        val percentBD = BigDecimal(commissionPercent.toString())
        return totalBD.multiply(percentBD)
            .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
            .toInt()
    }

    fun netIncome(total: Int, commissionPercent: Float): Int {
        return total - commissionAmount(total, commissionPercent)
    }

    fun balance(netIncome: Int, totalPaid: Int): Int {
        return netIncome - totalPaid
    }
}
