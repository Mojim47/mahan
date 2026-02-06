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
            parentColumns = ["name"],
            childColumns = ["driver"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["name"],
            childColumns = ["customer"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = Neighborhood::class,
            parentColumns = ["name"],
            childColumns = ["neighborhood"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["driver"]),
        Index(value = ["date_time"]),
        Index(value = ["settled"])
    ]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customer: String,
    val driver: String,
    val neighborhood: String,
    val amount: Int,
    val description: String,
    @ColumnInfo(name = "date_time")
    val dateTime: String,
    val settled: Boolean,
    val status: String = "pending"
)
