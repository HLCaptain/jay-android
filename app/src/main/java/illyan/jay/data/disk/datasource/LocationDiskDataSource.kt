package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.LocationDao
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDiskDataSource @Inject constructor(
    private val locationDao: LocationDao
) {
    fun getLocations(sessionId: Long, limit: Long) = locationDao.getLocations(sessionId, limit)
	    .map { it.map(RoomLocation::toDomainModel) }

	fun getLocations(sessionId: Long) = locationDao.getLocations(sessionId)
		.map { it.map(RoomLocation::toDomainModel) }

	fun saveLocation(location: DomainLocation) = locationDao.insertLocation(location.toRoomModel())
}