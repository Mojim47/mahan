package moji.deliverytracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private fun dbDateTimeFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private fun dbDateOnlyFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun displayDateFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    private fun displayTimeFormat(): SimpleDateFormat =
        SimpleDateFormat("HH:mm", Locale.getDefault())

    fun nowDb(): String = dbDateTimeFormat().format(Date())

    fun formatDb(date: Date): String = dbDateTimeFormat().format(date)

    fun todayPrefixDb(): String = dbDateOnlyFormat().format(Date())

    fun formatDisplayDate(date: Date): String = displayDateFormat().format(date)

    fun formatDisplayTime(date: Date): String = displayTimeFormat().format(date)
}
