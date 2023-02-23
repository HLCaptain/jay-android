package illyan.jay.data.disk.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import illyan.jay.data.disk.model.RoomPreferences
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface PreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertPreferences(preferences: RoomPreferences): Long

    @Delete
    fun deletePreferences(preferences: RoomPreferences)

    @Query("SELECT * FROM preferences WHERE userUUID IS :userUUID")
    fun getPreferences(userUUID: String): Flow<RoomPreferences?>

    @Query("DELETE FROM preferences WHERE userUUID IS :userUUID")
    fun deletePreferences(userUUID: String)

    @Query("UPDATE preferences SET analyticsEnabled = :analyticsEnabled, lastUpdate = :lastUpdate WHERE userUUID IS :userUUID")
    fun setAnalyticsEnabled(userUUID: String, analyticsEnabled: Boolean, lastUpdate: Long = Instant.now().toEpochMilli())

    @Query("UPDATE preferences SET freeDriveAutoStart = :freeDriveAutoStart, lastUpdate = :lastUpdate WHERE userUUID IS :userUUID")
    fun setFreeDriveAutoStart(userUUID: String, freeDriveAutoStart: Boolean, lastUpdate: Long = Instant.now().toEpochMilli())
}