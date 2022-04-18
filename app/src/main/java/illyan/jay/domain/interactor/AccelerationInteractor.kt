package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.AccelerationDiskDataSource
import illyan.jay.domain.model.DomainAcceleration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccelerationInteractor @Inject constructor(
    private val accelerationDiskDataSource: AccelerationDiskDataSource
) {
    fun saveAcceleration(acceleration: DomainAcceleration) =
        accelerationDiskDataSource.saveAcceleration(acceleration)
}