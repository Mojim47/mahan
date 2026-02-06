package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Query(
        """
        SELECT p.id, p.driver_id AS driverId, p.amount, p.payment_method AS method, p.date_time AS dateTime,
               d.name AS driverName
        FROM payments p
        JOIN drivers d ON d.id = p.driver_id
        ORDER BY p.id DESC
        """
    )
    fun getAllWithDriverFlow(): Flow<List<PaymentWithDriverName>>

    @Query(
        """
        SELECT p.id, p.driver_id AS driverId, p.amount, p.payment_method AS method, p.date_time AS dateTime,
               d.name AS driverName
        FROM payments p
        JOIN drivers d ON d.id = p.driver_id
        ORDER BY p.id DESC
        """
    )
    suspend fun getAllWithDriverOnce(): List<PaymentWithDriverName>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId")
    suspend fun getTotalPaid(driverId: Int): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId")
    fun getTotalPaidFlow(driverId: Int): Flow<Int>
}
