package illyan.jay.data.disk.dao

import androidx.room.*
import illyan.jay.data.disk.model.RoomLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    fun insertLocation(location: RoomLocation): Long

    @Query("SELECT * FROM location")
    fun getLocations(): Flow<List<RoomLocation>>

    @Update
    fun updateLocation(location: RoomLocation): Int

    @Update
    fun updateLocation(location: List<RoomLocation>): Int

    @Delete
    fun deleteLocation(location: RoomLocation)

    @Query("SELECT * FROM location WHERE id = :id")
    fun getLocation(id: Long): RoomLocation?

    @Query("SELECT * FROM location WHERE sessionId = :sessionId")
    fun getLocations(sessionId: Long): Flow<List<RoomLocation>>

    @Delete
    fun deleteLocations(locations: List<RoomLocation>)

    @Query("DELETE FROM location")
    fun deleteLocations()

    @Query("DELETE FROM location WHERE sessionId = :sessionId")
    fun deleteLocations(sessionId: Long)

    @Query("SELECT * FROM location WHERE sessionId = :sessionId ORDER BY id DESC LIMIT :limit")
    fun getLocations(sessionId: Long, limit: Long): Flow<List<RoomLocation>>
}