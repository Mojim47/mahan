package moji.deliverytracker

data class OrderWithNames(
    val id: Int,
    val customerId: Int,
    val driverId: Int,
    val neighborhoodId: Int,
    val amount: Int,
    val description: String,
    val dateTime: String,
    val settled: Boolean,
    val status: String,
    val customerName: String,
    val driverName: String,
    val neighborhoodName: String
)
