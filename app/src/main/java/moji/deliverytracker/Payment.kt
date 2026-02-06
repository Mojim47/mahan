package moji.deliverytracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val driver: String,
    val amount: Int,
    @ColumnInfo(name = "payment_method")
    val method: String,
    @ColumnInfo(name = "date_time")
    val dateTime: String
)
