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

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.RotationDao
import illyan.jay.data.disk.model.RoomRotation
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainRotation
import illyan.jay.domain.model.DomainSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

/**
 * Rotation disk data source using Room to communicate with the SQLite database.
 *
 * @property rotationDao used to insert, update, delete and query commands using Room.
 * @constructor Create empty Rotation disk data source
 */
@Singleton
class RotationDiskDataSource @Inject constructor(
    private val rotationDao: RotationDao
) {
    /**
     * Get rotations' data as a Flow for a particular session.
     *
     * @param session particular session, whose ID is the
     * foreign key of rotation data returned.
     *
     * @return rotation data for a particular session.
     */
    fun getRotations(session: DomainSession) = getRotations(session.id)

    /**
     * Get rotations' data as a Flow for a particular session.
     *
     * @param sessionId particular session's ID, which is the
     * foreign key of rotation data returned.
     *
     * @return rotation data flow for a particular session.
     */
    fun getRotations(sessionId: Long) =
        rotationDao.getRotations(sessionId).map { it.map(RoomRotation::toDomainModel) }

    /**
     * Save rotation's data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param rotation rotation data saved onto the Room database.
     *
     * @return id of the saved rotation.
     */
    fun saveRotation(rotation: DomainRotation) =
        rotationDao.upsertRotation(rotation.toRoomModel())

    /**
     * Save rotation's data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param rotations list of rotation data saved onto the Room database.
     */
    fun saveRotations(rotations: List<DomainRotation>) =
        rotationDao.upsertRotations(rotations.map(DomainRotation::toRoomModel))

    fun deleteRotationsForSession(sessionId: Long) =
        rotationDao.deleteRotationsForSession(sessionId)
}
