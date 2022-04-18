package illyan.jay.data.disk.dao

import androidx.room.*
import illyan.jay.data.disk.model.RoomAcceleration
import kotlinx.coroutines.flow.Flow

@Dao
interface AccelerationDao {
    @Insert
    fun insertAcceleration(acceleration: RoomAcceleration): Long

    @Query("SELECT * FROM acceleration")
    fun getAccelerations(): Flow<List<RoomAcceleration>>

    @Update
    fun updateAcceleration(acceleration: RoomAcceleration): Int

    @Delete
    fun deleteAcceleration(acceleration: RoomAcceleration)

    @Query("SELECT * FROM acceleration WHERE id = :id")
    fun getAcceleration(id: Int): RoomAcceleration?

    @Query("SELECT * FROM acceleration WHERE sessionId = :sessionId")
    fun getAccelerations(sessionId: Int): Flow<List<RoomAcceleration>>

    @Delete
    fun deleteAccelerations(accelerations: List<RoomAcceleration>)

    @Query("DELETE FROM acceleration")
    fun deleteAccelerations()

    @Query("DELETE FROM acceleration WHERE sessionId = :sessionId")
    fun deleteAccelerations(sessionId: Int)
}