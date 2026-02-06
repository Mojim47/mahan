package moji.deliverytracker

data class PaymentWithDriverName(
    val id: Int,
    val driverId: Int,
    val amount: Int,
    val method: String,
    val dateTime: String,
    val driverName: String
)
