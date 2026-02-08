package moji.deliverytracker

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
    private fun dayFormat(): java.text.SimpleDateFormat =
        java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun orderFormat(): java.text.SimpleDateFormat =
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun filterOrders(orders: List<Order>, period: ReportPeriod, now: Calendar = Calendar.getInstance()): List<Order> {
        val todayKey = dayFormat().format(now.time)
        val nowTime = now.timeInMillis

        return orders.filter { order ->
            val orderDate = orderFormat().parse(order.dateTime) ?: Date(0)
            val orderCal = Calendar.getInstance().apply { time = orderDate }
            val orderTime = orderCal.timeInMillis

            when (period) {
                ReportPeriod.TODAY -> dayFormat.format(orderCal.time) == todayKey && orderTime <= nowTime
                ReportPeriod.WEEK -> {
                    val weekAgo = Calendar.getInstance().apply { time = now.time; add(Calendar.DAY_OF_YEAR, -7) }
                    orderTime in weekAgo.timeInMillis..nowTime
                }
                ReportPeriod.MONTH -> {
                    val monthAgo = Calendar.getInstance().apply { time = now.time; add(Calendar.MONTH, -1) }
                    orderTime in monthAgo.timeInMillis..nowTime
                }
            }
        }
    }

    fun calculateSummary(orders: List<Order>, drivers: List<Driver>): ReportSummary {
        val commissionByDriver = drivers.associate { it.id to it.commission }
        var totalCommission = 0

        orders.forEach { order ->
            val commission = commissionByDriver[order.driverId] ?: 0f
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
