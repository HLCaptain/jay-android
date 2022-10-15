/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.disk

import androidx.room.Database
import androidx.room.RoomDatabase
import illyan.jay.data.disk.dao.LocationDao
import illyan.jay.data.disk.dao.SensorEventDao
import illyan.jay.data.disk.dao.SessionDao
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.data.disk.model.RoomSensorEvent
import illyan.jay.data.disk.model.RoomSession

/**
 * Jay abstract database class annotated for generated code
 * to create SQLite implementations.
 *
 * @constructor Create empty Jay database
 */
@Database(
    entities = [
        RoomSession::class,
        RoomLocation::class,
        RoomSensorEvent::class
    ],
    version = 11,
    exportSchema = false
)
abstract class JayDatabase : RoomDatabase() {
    /**
     * Room's generated code implements this method.
     * Session Dao helps with managing sessions, which are essentially
     * an interval of time spent collecting data.
     *
     * @return SessionDao
     */
    abstract fun sessionDao(): SessionDao

    /**
     * Room's generated code implements this method.
     *
     * @return LocationDao
     */
    abstract fun locationDao(): LocationDao

    /**
     * Room's generated code implements this method.
     *
     * @return SensorEventDao
     */
    abstract fun sensorEventDao(): SensorEventDao

    companion object {
        const val DB_NAME = "jay.db"
    }
}
