/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.RotationDao
import illyan.jay.data.disk.model.RoomRotation
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainRotation
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RotationDiskDataSource @Inject constructor(
	private val rotationDao: RotationDao
) {
	fun getRotations(session: DomainSession) = getRotations(session.id)
	fun getRotations(sessionId: Long) =
		rotationDao.getRotations(sessionId).map { it.map(RoomRotation::toDomainModel) }

	fun saveRotation(rotation: DomainRotation) =
		rotationDao.insertRotation(rotation.toRoomModel())
}