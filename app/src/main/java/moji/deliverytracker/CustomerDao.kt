package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer): Int

    @Query("DELETE FROM customers WHERE name = :name")
    suspend fun deleteByName(name: String): Int

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("SELECT * FROM customers WHERE name != '__UNKNOWN__' ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name != '__UNKNOWN__' ORDER BY name ASC")
    suspend fun getAllOnce(): List<Customer>

    @Query("SELECT name FROM customers WHERE name != '__UNKNOWN__' ORDER BY name ASC")
    fun getNamesFlow(): Flow<List<String>>

    @Query("SELECT name FROM customers WHERE name != '__UNKNOWN__' ORDER BY name ASC")
    suspend fun getNamesOnce(): List<String>

    @Query("SELECT id FROM customers WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Int?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Customer?

    @Query("SELECT COUNT(*) FROM customers WHERE name != '__UNKNOWN__'")
    fun getCountFlow(): Flow<Int>
}
