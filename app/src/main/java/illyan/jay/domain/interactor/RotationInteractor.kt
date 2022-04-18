package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.RotationDiskDataSource
import illyan.jay.domain.model.DomainRotation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RotationInteractor @Inject constructor(
    private var rotationDiskDataSource: RotationDiskDataSource
) {
    fun saveRotation(rotation: DomainRotation) = rotationDiskDataSource.saveRotation(rotation)
}