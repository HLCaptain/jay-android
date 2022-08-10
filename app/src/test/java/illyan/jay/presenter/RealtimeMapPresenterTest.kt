/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.presenter

import illyan.jay.TestBase
import illyan.jay.domain.interactor.SensorInteractor
import illyan.jay.ui.realtime_map.RealtimeMapPresenter
import illyan.jay.ui.realtime_map.model.UiLocation
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RealtimeMapPresenterTest : TestBase() {

	private lateinit var mockedSensorInteractor: SensorInteractor
	private lateinit var realtimeMapPresenter: RealtimeMapPresenter

	@BeforeEach
	fun initEach() {
		mockedSensorInteractor = mockk(relaxed = true)
		realtimeMapPresenter = RealtimeMapPresenter(mockedSensorInteractor)
	}

	@Test
	fun `Request location updates from sensor interactor`() {
		val stateListener: (UiLocation) -> Unit = { _ -> }

		realtimeMapPresenter.setLocationListener(stateListener)

		verify(exactly = 1) { mockedSensorInteractor.requestLocationUpdates(any(), any()) }
	}

	@Test
	fun `Request location updates from sensor interactor, then remove it`() {
		val stateListener: (UiLocation) -> Unit = { _ -> }

		realtimeMapPresenter.setLocationListener(stateListener)
		realtimeMapPresenter.stopListening()

		verify(exactly = 1) { mockedSensorInteractor.requestLocationUpdates(any(), any()) }
		verify(exactly = 1) { mockedSensorInteractor.removeLocationUpdates(any()) }
		verifySequence {
			mockedSensorInteractor.requestLocationUpdates(any(), any())
			mockedSensorInteractor.removeLocationUpdates(any())
		}
	}
}