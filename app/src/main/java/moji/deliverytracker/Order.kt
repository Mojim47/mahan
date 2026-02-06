package moji.deliverytracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Driver::class,
            parentColumns = ["id"],
            childColumns = ["driver_id"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = Neighborhood::class,
            parentColumns = ["id"],
            childColumns = ["neighborhood_id"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["driver_id"]),
        Index(value = ["customer_id"]),
        Index(value = ["neighborhood_id"]),
        Index(value = ["date_time"]),
        Index(value = ["settled"])
    ]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "customer_id")
    val customerId: Int,
    @ColumnInfo(name = "driver_id")
    val driverId: Int,
    @ColumnInfo(name = "neighborhood_id")
    val neighborhoodId: Int,
    val amount: Int,
    val description: String,
    @ColumnInfo(name = "date_time")
    val dateTime: String,
    val settled: Boolean,
    @ColumnInfo(name = "settled_at")
    val settledAt: String? = null,
    val status: String = "pending"
)
