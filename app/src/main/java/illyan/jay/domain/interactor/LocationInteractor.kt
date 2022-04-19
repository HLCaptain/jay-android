package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.domain.model.DomainLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationInteractor @Inject constructor(
    private var locationDiskDataSource: LocationDiskDataSource
) {
	companion object {
		const val LOCATION_REQUEST_INTERVAL_REALTIME = 200L
		const val LOCATION_REQUEST_INTERVAL_FREQUENT = 500L
		const val LOCATION_REQUEST_INTERVAL_DEFAULT = 2000L
		const val LOCATION_REQUEST_INTERVAL_SPARSE = 4000L

		const val LOCATION_REQUEST_DISPLACEMENT_MOST_ACCURATE = 1f
		const val LOCATION_REQUEST_DISPLACEMENT_DEFAULT = 4f
		const val LOCATION_REQUEST_DISPLACEMENT_LEAST_ACCURATE = 8f
	}

	fun getLocations(sessionId: Long, limit: Long) =
		locationDiskDataSource.getLocations(sessionId, limit)

	fun getLocations(sessionId: Long) = locationDiskDataSource.getLocations(sessionId)
	fun saveLocation(location: DomainLocation) = locationDiskDataSource.saveLocation(location)
}