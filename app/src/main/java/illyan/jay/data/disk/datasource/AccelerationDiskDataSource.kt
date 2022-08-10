/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.AccelerationDao
import illyan.jay.data.disk.model.RoomAcceleration
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainAcceleration
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acceleration disk data source using Room to communicate with the SQLite database.
 *
 * @property accelerationDao used to insert, update, delete and query commands using Room.
 * @constructor Create empty Acceleration disk data source
 */
@Singleton
class AccelerationDiskDataSource @Inject constructor(
	private val accelerationDao: AccelerationDao
) {
	/**
	 * Get accelerations' data as a Flow for a particular session.
	 *
	 * @param session particular session, whose ID is the
	 * foreign key of acceleration data returned.
	 *
	 * @return acceleration data for a particular session.
	 */
	fun getAccelerations(session: DomainSession) = getAccelerations(session.id)

	/**
	 * Get accelerations' data as a Flow for a particular session.
	 *
	 * @param sessionId particular session's ID, which is the
	 * foreign key of acceleration data returned.
	 *
	 * @return acceleration data flow for a particular session.
	 */
	fun getAccelerations(sessionId: Long) =
		accelerationDao.getAccelerations(sessionId)
			.map { it.map(RoomAcceleration::toDomainModel) }

	/**
	 * Save acceleration's data to Room database.
	 * Should be linked to a session to be accessible later on.
	 *
	 * @param acceleration acceleration data saved onto the Room database.
	 *
	 * @return id of the saved acceleration.
	 */
	fun saveAcceleration(acceleration: DomainAcceleration) =
		accelerationDao.upsertAcceleration(acceleration.toRoomModel())

	/**
	 * Save accelerations' data to Room database.
	 * Should be linked to a session to be accessible later on.
	 *
	 * @param accelerations list of acceleration data saved onto the Room database.
	 */
	fun saveAccelerations(accelerations: List<DomainAcceleration>) =
		accelerationDao.upsertAccelerations(accelerations.map(DomainAcceleration::toRoomModel))

	fun deleteAccelerationsForSession(sessionId: Long) = accelerationDao.deleteAccelerationsForSession(sessionId)
}