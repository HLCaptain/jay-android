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
import illyan.jay.domain.interactor.ServiceInteractor
import illyan.jay.ui.toggle.service.ServiceTogglePresenter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ServiceTogglePresenterTest : TestBase() {

	private lateinit var mockedServiceInteractor: ServiceInteractor
	private lateinit var serviceTogglePresenter: ServiceTogglePresenter

	@BeforeEach
	fun initEach() {
		mockedServiceInteractor = mockk(relaxed = true)
		serviceTogglePresenter = ServiceTogglePresenter(mockedServiceInteractor)
	}

	@Test
	fun `Start Jay service`() {
		serviceTogglePresenter.startService()

		verify(exactly = 1) { mockedServiceInteractor.startJayService() }
	}

	@Test
	fun `Stop Jay service`() {
		serviceTogglePresenter.stopService()

		verify(exactly = 1) { mockedServiceInteractor.stopJayService() }
	}

	@ParameterizedTest(name = "Is Jay service running? {0}")
	@ValueSource(booleans = [true, false])
	fun `Check if Jay service is running`(isJayServiceRunning: Boolean) {
		every { mockedServiceInteractor.isJayServiceRunning() } returns isJayServiceRunning

		val result = serviceTogglePresenter.isJayServiceRunning()

		assertEquals(isJayServiceRunning, result)
		verify(exactly = 1) { mockedServiceInteractor.isJayServiceRunning() }
	}

	@Test
	fun `Add Jay service state listener`() {
		val stateListener: (Boolean, String) -> Unit = { _, _ -> }

		serviceTogglePresenter.addJayServiceStateListener(stateListener)

		verify(exactly = 1) { mockedServiceInteractor.addServiceStateListener(any())}
	}
}