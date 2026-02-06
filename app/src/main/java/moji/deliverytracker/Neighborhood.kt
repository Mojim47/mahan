package moji.deliverytracker

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "neighborhoods",
    indices = [Index(value = ["name"], unique = true)]
)
data class Neighborhood(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
