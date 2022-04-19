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