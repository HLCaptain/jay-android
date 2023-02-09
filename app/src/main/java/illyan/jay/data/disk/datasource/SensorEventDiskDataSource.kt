/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.SensorEventDao
import illyan.jay.data.disk.model.RoomSensorEvent
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SensorEvent disk data source using Room to communicate with the SQLite database.
 *
 * @property sensorEventDao used to insert, update, delete and query commands using Room.
 * @constructor Create empty SensorEvent disk data source
 */
@Singleton
class SensorEventDiskDataSource @Inject constructor(
    private val sensorEventDao: SensorEventDao
) {
    /**
     * Get sensorEvents' data as a Flow for a particular session.
     *
     * @param session particular session, whose ID is the
     * foreign key of sensorEvent data returned.
     *
     * @return sensorEvent data for a particular session.
     */
    fun getSensorEvents(session: DomainSession) = getSensorEvents(session.uuid)

    /**
     * Get sensorEvents' data as a Flow for a particular session.
     *
     * @param sessionUUID particular session's ID, which is the
     * foreign key of sensorEvent data returned.
     *
     * @return sensorEvent data flow for a particular session.
     */
    fun getSensorEvents(sessionUUID: String) =
        sensorEventDao.getSensorEvents(sessionUUID)
            .map { it.map(RoomSensorEvent::toDomainModel) }

    /**
     * Save sensorEvent's data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param sensorEvent sensorEvent data saved onto the Room database.
     *
     * @return id of the saved sensorEvent.
     */
    fun saveSensorEvent(sensorEvent: DomainSensorEvent) =
        sensorEventDao.upsertSensorEvent(sensorEvent.toRoomModel())

    /**
     * Save sensorEvents' data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param sensorEvents list of sensorEvent data saved onto the Room database.
     */
    fun saveSensorEvents(sensorEvents: List<DomainSensorEvent>) =
        sensorEventDao.upsertSensorEvents(sensorEvents.map(DomainSensorEvent::toRoomModel))

    fun deleteSensorEventsForSession(sessionUUID: String) =
        sensorEventDao.deleteSensorEventsForSession(sessionUUID)
}
