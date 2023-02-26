/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

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

    @Query("UPDATE preferences SET shouldSync = :shouldSync WHERE userUUID IS :userUUID")
    fun setShouldSync(userUUID: String, shouldSync: Boolean)
}