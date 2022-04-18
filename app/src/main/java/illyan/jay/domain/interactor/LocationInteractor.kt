package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.domain.model.DomainLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationInteractor @Inject constructor(
    private var locationDiskDataSource: LocationDiskDataSource
) {
    fun getLocations(sessionId: Int, limit: Int) = locationDiskDataSource.getLocations(sessionId, limit)
    fun getLocations(sessionId: Int) = locationDiskDataSource.getLocations(sessionId)
    fun saveLocation(location: DomainLocation) = locationDiskDataSource.saveLocation(location)
}