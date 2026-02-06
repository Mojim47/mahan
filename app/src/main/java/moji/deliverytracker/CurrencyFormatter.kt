package moji.deliverytracker

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale("fa", "IR")

    fun formatToman(amount: Int, label: String): String {
        return "${formatNumber(amount.toLong())} $label"
    }

    fun formatNumber(amount: Long): String {
        val formatter = NumberFormat.getInstance(locale).apply {
            isGroupingUsed = true
            maximumFractionDigits = 0
        }
        return formatter.format(amount)
    }
}
