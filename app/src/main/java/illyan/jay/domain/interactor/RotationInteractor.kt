/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.RotationDiskDataSource
import illyan.jay.domain.model.DomainRotation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rotation interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property rotationDiskDataSource local datasource
 * @constructor Create empty Rotation interactor
 */
@Singleton
class RotationInteractor @Inject constructor(
	private var rotationDiskDataSource: RotationDiskDataSource
) {
	/**
	 * Save a rotation data instance.
	 *
	 * @param rotation rotation data to be saved.
	 *
	 * @return id of rotation updated.
	 */
	fun saveRotation(rotation: DomainRotation) = rotationDiskDataSource.saveRotation(rotation)

	/**
	 * Save multiple rotation data instance.
	 *
	 * @param rotations multiple rotations to be saved.
	 */
	fun saveRotations(rotations: List<DomainRotation>) =
		rotationDiskDataSource.saveRotations(rotations)
}