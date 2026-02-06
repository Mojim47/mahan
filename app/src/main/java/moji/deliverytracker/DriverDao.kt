package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(driver: Driver): Long

    @Update
    suspend fun update(driver: Driver): Int

    @Query("DELETE FROM drivers WHERE name = :name")
    suspend fun deleteByName(name: String): Int

    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers ORDER BY name ASC")
    suspend fun getAllOnce(): List<Driver>

    @Query("SELECT name FROM drivers ORDER BY name ASC")
    fun getNamesFlow(): Flow<List<String>>

    @Query("SELECT name FROM drivers ORDER BY name ASC")
    suspend fun getNamesOnce(): List<String>

    @Query("SELECT commission FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getCommissionById(id: Int): Float?

    @Query("SELECT commission FROM drivers WHERE id = :id LIMIT 1")
    fun getCommissionFlow(id: Int): Flow<Float?>

    @Query("SELECT id FROM drivers WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Int?

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Driver?

    @Query("SELECT COUNT(*) FROM drivers")
    fun getCountFlow(): Flow<Int>
}
