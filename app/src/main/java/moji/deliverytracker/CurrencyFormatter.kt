package moji.deliverytracker

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale("fa", "IR")

    private val formatter: NumberFormat by lazy {
        NumberFormat.getInstance(locale).apply {
            isGroupingUsed = true
            maximumFractionDigits = 0
        }
    }

    fun formatToman(amount: Int, label: String): String {
        return "${formatNumber(amount.toLong())} $label"
    }

    fun formatNumber(amount: Long): String {
        return synchronized(formatter) {
            formatter.format(amount)
        }
    }
}
