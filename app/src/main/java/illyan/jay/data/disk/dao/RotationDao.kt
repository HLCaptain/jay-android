package illyan.jay.data.disk.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import illyan.jay.data.disk.model.RoomRotation
import kotlinx.coroutines.flow.Flow

@Dao
interface RotationDao {
    @Insert
    fun insertRotation(rotation: RoomRotation): Long

    @Query("SELECT * FROM rotation")
    fun getRotations(): Flow<List<RoomRotation>>

    @Update
    fun updateRotation(rotation: RoomRotation): Int

    @Delete
    fun deleteRotation(rotation: RoomRotation)

    @Query("SELECT * FROM rotation WHERE id = :id")
    fun getRotation(id: Int): RoomRotation?

    @Query("SELECT * FROM rotation WHERE sessionId = :sessionId")
    fun getRotations(sessionId: Int): Flow<List<RoomRotation>>

    @Delete
    fun deleteRotations(rotations: List<RoomRotation>)

    @Query("DELETE FROM rotation")
    fun deleteRotations()

    @Query("DELETE FROM rotation WHERE sessionId = :sessionId")
    fun deleteRotations(sessionId: Int)
}