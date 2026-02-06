package moji.deliverytracker

import java.text.SimpleDateFormat
import java.util.*

enum class ReportPeriod {
    TODAY,
    WEEK,
    MONTH
}

data class ReportSummary(
    val totalOrders: Int,
    val totalSales: Int,
    val totalCommission: Int,
    val netIncome: Int,
    val settled: Int,
    val unsettled: Int
)

object ReportCalculator {
    fun filterOrders(orders: List<Order>, period: ReportPeriod, now: Calendar = Calendar.getInstance()): List<Order> {
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val orderFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val todayKey = dayFormat.format(now.time)

        return orders.filter { order ->
            val orderDate = orderFormat.parse(order.dateTime) ?: Date(0)
            val orderCal = Calendar.getInstance().apply { time = orderDate }

            when (period) {
                ReportPeriod.TODAY -> dayFormat.format(orderCal.time) == todayKey
                ReportPeriod.WEEK -> {
                    val weekAgo = Calendar.getInstance().apply { time = now.time; add(Calendar.DAY_OF_YEAR, -7) }
                    orderCal.after(weekAgo)
                }
                ReportPeriod.MONTH -> {
                    val monthAgo = Calendar.getInstance().apply { time = now.time; add(Calendar.MONTH, -1) }
                    orderCal.after(monthAgo)
                }
            }
        }
    }

    fun calculateSummary(orders: List<Order>, drivers: List<Driver>): ReportSummary {
        val commissionByDriver = drivers.associate { it.name to it.commission }
        var totalCommission = 0

        orders.forEach { order ->
            val commission = commissionByDriver[order.driver] ?: 0f
            totalCommission += MoneyCalculator.commissionAmount(order.amount, commission)
        }

        val totalOrders = orders.size
        val totalSales = orders.sumOf { it.amount }
        val settled = orders.filter { it.settled }.sumOf { it.amount }
        val unsettled = orders.filter { !it.settled }.sumOf { it.amount }
        val netIncome = totalSales - totalCommission

        return ReportSummary(
            totalOrders = totalOrders,
            totalSales = totalSales,
            totalCommission = totalCommission,
            netIncome = netIncome,
            settled = settled,
            unsettled = unsettled
        )
    }
}
