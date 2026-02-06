package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order): Int

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY id DESC")
    suspend fun getAllOnce(): List<Order>

    @Query("SELECT * FROM orders WHERE driver = :driver ORDER BY id DESC")
    suspend fun getByDriverOnce(driver: String): List<Order>

    @Query("SELECT * FROM orders WHERE driver = :driver ORDER BY id DESC")
    fun getByDriverFlow(driver: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Order?

    @Query("UPDATE orders SET settled = :settled WHERE driver = :driver")
    suspend fun updateSettledForDriver(driver: String, settled: Boolean): Int

    @Query("SELECT COALESCE(SUM(amount), 0) AS total, COUNT(*) AS count FROM orders WHERE date_time LIKE :datePrefix")
    fun getSummaryFlow(datePrefix: String): Flow<OrderSummary>
}
