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

    @Query(
        """
        SELECT o.id, o.customer_id AS customerId, o.driver_id AS driverId, o.neighborhood_id AS neighborhoodId,
               o.amount, o.description, o.date_time AS dateTime, o.settled, o.settled_at AS settledAt, o.status,
               COALESCE(c.name, '__UNKNOWN__') AS customerName,
               COALESCE(d.name, '__UNKNOWN__') AS driverName,
               COALESCE(n.name, '__UNKNOWN__') AS neighborhoodName
        FROM orders o
        LEFT JOIN customers c ON c.id = o.customer_id
        LEFT JOIN drivers d ON d.id = o.driver_id
        LEFT JOIN neighborhoods n ON n.id = o.neighborhood_id
        ORDER BY o.id DESC
        """
    )
    fun getAllWithNamesFlow(): Flow<List<OrderWithNames>>

    @Query(
        """
        SELECT o.id, o.customer_id AS customerId, o.driver_id AS driverId, o.neighborhood_id AS neighborhoodId,
               o.amount, o.description, o.date_time AS dateTime, o.settled, o.settled_at AS settledAt, o.status,
               COALESCE(c.name, '__UNKNOWN__') AS customerName,
               COALESCE(d.name, '__UNKNOWN__') AS driverName,
               COALESCE(n.name, '__UNKNOWN__') AS neighborhoodName
        FROM orders o
        LEFT JOIN customers c ON c.id = o.customer_id
        LEFT JOIN drivers d ON d.id = o.driver_id
        LEFT JOIN neighborhoods n ON n.id = o.neighborhood_id
        ORDER BY o.id DESC
        """
    )
    suspend fun getAllWithNamesOnce(): List<OrderWithNames>

    @Query(
        """
        SELECT o.id, o.customer_id AS customerId, o.driver_id AS driverId, o.neighborhood_id AS neighborhoodId,
               o.amount, o.description, o.date_time AS dateTime, o.settled, o.settled_at AS settledAt, o.status,
               COALESCE(c.name, '__UNKNOWN__') AS customerName,
               COALESCE(d.name, '__UNKNOWN__') AS driverName,
               COALESCE(n.name, '__UNKNOWN__') AS neighborhoodName
        FROM orders o
        LEFT JOIN customers c ON c.id = o.customer_id
        LEFT JOIN drivers d ON d.id = o.driver_id
        LEFT JOIN neighborhoods n ON n.id = o.neighborhood_id
        WHERE o.driver_id = :driverId
        ORDER BY o.id DESC
        """
    )
    fun getByDriverWithNamesFlow(driverId: Int): Flow<List<OrderWithNames>>

    @Query(
        """
        SELECT o.id, o.customer_id AS customerId, o.driver_id AS driverId, o.neighborhood_id AS neighborhoodId,
               o.amount, o.description, o.date_time AS dateTime, o.settled, o.settled_at AS settledAt, o.status,
               COALESCE(c.name, '__UNKNOWN__') AS customerName,
               COALESCE(d.name, '__UNKNOWN__') AS driverName,
               COALESCE(n.name, '__UNKNOWN__') AS neighborhoodName
        FROM orders o
        LEFT JOIN customers c ON c.id = o.customer_id
        LEFT JOIN drivers d ON d.id = o.driver_id
        LEFT JOIN neighborhoods n ON n.id = o.neighborhood_id
        WHERE o.driver_id = :driverId AND o.settled = 0
        ORDER BY o.id DESC
        """
    )
    fun getUnsettledByDriverWithNamesFlow(driverId: Int): Flow<List<OrderWithNames>>

    @Query(
        """
        SELECT o.id, o.customer_id AS customerId, o.driver_id AS driverId, o.neighborhood_id AS neighborhoodId,
               o.amount, o.description, o.date_time AS dateTime, o.settled, o.settled_at AS settledAt, o.status,
               COALESCE(c.name, '__UNKNOWN__') AS customerName,
               COALESCE(d.name, '__UNKNOWN__') AS driverName,
               COALESCE(n.name, '__UNKNOWN__') AS neighborhoodName
        FROM orders o
        LEFT JOIN customers c ON c.id = o.customer_id
        LEFT JOIN drivers d ON d.id = o.driver_id
        LEFT JOIN neighborhoods n ON n.id = o.neighborhood_id
        WHERE o.id = :id
        LIMIT 1
        """
    )
    suspend fun getByIdWithNames(id: Int): OrderWithNames?

    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY id DESC")
    suspend fun getAllOnce(): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Order?

    @Query("UPDATE orders SET settled = :settled, settled_at = :settledAt WHERE driver_id = :driverId AND settled = 0")
    suspend fun updateSettledForDriver(driverId: Int, settled: Boolean, settledAt: String): Int

    @Query("SELECT MAX(settled_at) FROM orders WHERE driver_id = :driverId AND settled = 1")
    fun getLastSettlementTimeFlow(driverId: Int): Flow<String?>

    @Query("SELECT COALESCE(SUM(amount), 0) AS total, COUNT(*) AS count FROM orders WHERE date_time LIKE :datePrefix")
    fun getSummaryFlow(datePrefix: String): Flow<OrderSummary>

    @Query("SELECT COUNT(*) FROM orders WHERE driver_id = :driverId")
    suspend fun countByDriver(driverId: Int): Int

    @Query("SELECT COUNT(*) FROM orders WHERE customer_id = :customerId")
    suspend fun countByCustomer(customerId: Int): Int

    @Query("SELECT COUNT(*) FROM orders WHERE neighborhood_id = :neighborhoodId")
    suspend fun countByNeighborhood(neighborhoodId: Int): Int

    @Query("SELECT * FROM orders WHERE date_time >= :since ORDER BY id DESC")
    fun getOrdersSinceFlow(since: String): Flow<List<Order>>
}
