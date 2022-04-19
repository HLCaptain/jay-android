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

@Singleton
class AccelerationDiskDataSource @Inject constructor(
    private val accelerationDao: AccelerationDao
) {
    fun getAccelerations(session: DomainSession) = getAccelerations(session.id)
    fun getAccelerations(sessionId: Long) =
        accelerationDao.getAccelerations(sessionId)
            .map { it.map(RoomAcceleration::toDomainModel) }

    fun saveAcceleration(acceleration: DomainAcceleration) =
        accelerationDao.insertAcceleration(acceleration.toRoomModel())
}