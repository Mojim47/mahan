package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface CommissionHistoryDao {
    @Insert
    suspend fun insert(history: CommissionHistory): Long
}
