package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    suspend fun getAllOnce(): List<Payment>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver = :driver")
    suspend fun getTotalPaid(driver: String): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver = :driver")
    fun getTotalPaidFlow(driver: String): Flow<Int>
}
