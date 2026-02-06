package moji.deliverytracker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class ReportCalculatorTest {

    @Test
    fun calculateSummary_aggregatesTotals() {
        val drivers = listOf(
            Driver(1, "Ali", "", "", "", "", 10f),
            Driver(2, "Sara", "", "", "", "", 20f)
        )

        val orders = listOf(
            Order(1, 1, 1, 1, 100, "", "2026-02-06 10:00:00", true),
            Order(2, 2, 2, 1, 200, "", "2026-02-06 11:00:00", false),
            Order(3, 3, 2, 2, 50, "", "2026-02-05 09:00:00", true)
        )

        val summary = ReportCalculator.calculateSummary(orders, drivers)

        assertEquals(3, summary.totalOrders)
        assertEquals(350, summary.totalSales)
        assertEquals(60, summary.totalCommission)
        assertEquals(290, summary.netIncome)
        assertEquals(150, summary.settled)
        assertEquals(200, summary.unsettled)
    }

    @Test
    fun filterOrdersByPeriod_filtersCorrectly() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 6, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val orders = listOf(
            Order(1, 1, 1, 1, 100, "", sdf.format(Date(now.timeInMillis)), true),
            Order(2, 2, 1, 1, 100, "", sdf.format(Date(now.timeInMillis - 3L * 24 * 60 * 60 * 1000)), true),
            Order(3, 3, 1, 1, 100, "", sdf.format(Date(now.timeInMillis - 10L * 24 * 60 * 60 * 1000)), true),
            Order(4, 4, 1, 1, 100, "", sdf.format(Date(now.timeInMillis - 40L * 24 * 60 * 60 * 1000)), true)
        )

        assertEquals(1, ReportCalculator.filterOrders(orders, ReportPeriod.TODAY, now).size)
        assertEquals(2, ReportCalculator.filterOrders(orders, ReportPeriod.WEEK, now).size)
        assertEquals(3, ReportCalculator.filterOrders(orders, ReportPeriod.MONTH, now).size)
    }
}
