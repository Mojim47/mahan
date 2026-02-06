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

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllOnce(): List<Customer>

    @Query("SELECT name FROM customers ORDER BY name ASC")
    fun getNamesFlow(): Flow<List<String>>

    @Query("SELECT name FROM customers ORDER BY name ASC")
    suspend fun getNamesOnce(): List<String>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Customer?

    @Query("SELECT COUNT(*) FROM customers")
    fun getCountFlow(): Flow<Int>
}
