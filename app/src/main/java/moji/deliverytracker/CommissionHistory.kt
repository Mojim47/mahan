package moji.deliverytracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "commission_history",
    foreignKeys = [
        ForeignKey(
            entity = Driver::class,
            parentColumns = ["name"],
            childColumns = ["driver"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class CommissionHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val driver: String,
    @ColumnInfo(name = "old_commission")
    val oldCommission: Float,
    @ColumnInfo(name = "new_commission")
    val newCommission: Float,
    @ColumnInfo(name = "date_time")
    val dateTime: String
)
