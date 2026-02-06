package moji.deliverytracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drivers",
    indices = [Index(value = ["name"], unique = true)]
)
data class Driver(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "national_id")
    val nationalId: String,
    val plate: String,
    val phone: String,
    val address: String,
    val commission: Float
)
