package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NeighborhoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(neighborhood: Neighborhood): Long

    @Query("DELETE FROM neighborhoods WHERE name = :name")
    suspend fun deleteByName(name: String): Int

    @Query("SELECT name FROM neighborhoods ORDER BY name ASC")
    fun getNamesFlow(): Flow<List<String>>

    @Query("SELECT name FROM neighborhoods ORDER BY name ASC")
    suspend fun getNamesOnce(): List<String>

    @Query("SELECT id FROM neighborhoods WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Int?

    @Query("SELECT COUNT(*) FROM neighborhoods")
    fun getCountFlow(): Flow<Int>
}
