package illyan.jay.data.disk.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import illyan.jay.data.disk.model.RoomSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: RoomSession): Long

    @Query("SELECT * FROM session")
    fun getSessions(): Flow<List<RoomSession>>

    @Query("SELECT id FROM session")
    fun getSessionIds(): Flow<List<Int>>

    @Update
    fun updateSession(session: RoomSession): Int

    @Delete
    fun deleteSession(session: RoomSession)

    @Query("SELECT * FROM session WHERE id = :id LIMIT 1")
    fun getSession(id: Int): Flow<RoomSession?>

    @Query("SELECT * FROM session WHERE endTime is NULL")
    fun getOngoingSessions(): Flow<List<RoomSession>>

    @Query("SELECT id FROM session WHERE endTime is NULL")
    fun getOngoingSessionIds(): Flow<List<Int>>

    @Delete
    fun deleteSessions(sessions: List<RoomSession>)

    @Query("DELETE FROM session")
    fun deleteSessions()
}