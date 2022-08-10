/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.interactor

import android.content.ComponentName
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import illyan.jay.TestBase
import illyan.jay.domain.interactor.ServiceInteractor
import illyan.jay.service.ServiceStateReceiver
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceInteractorTest : TestBase() {
	private lateinit var mockedReceiver: ServiceStateReceiver
	private lateinit var mockedContext: Context
	private lateinit var mockedBroadcastManager: LocalBroadcastManager
	private lateinit var serviceInteractor: ServiceInteractor

	@BeforeEach
	fun initEach() {
		mockedReceiver = mockk()
		mockedContext = mockk()
		mockedBroadcastManager = mockk(relaxed = true)

		serviceInteractor = spyk(ServiceInteractor(mockedReceiver, mockedBroadcastManager, mockedContext))
	}

	@Test
	fun `Register service state receiver into the broadcast manager when initialized`() {
		verify(exactly = 1) { mockedBroadcastManager.registerReceiver(mockedReceiver, any()) }
	}

	@Test
	fun `Start Jay service while it is not running`() {
		val mockedComponentName = mockk<ComponentName>()
		every { serviceInteractor.isJayServiceRunning() } returns false
		every { mockedContext.startForegroundService(any()) } returns mockedComponentName

		val result = serviceInteractor.startJayService()

		assertEquals(mockedComponentName, result)
		verify(exactly = 1) { serviceInteractor.isJayServiceRunning() }
		verify(exactly = 1) { mockedContext.startForegroundService(any()) }
		verifySequence {
			serviceInteractor.startJayService()
			serviceInteractor.isJayServiceRunning()
			mockedContext.startForegroundService(any())
		}
	}

	@Test
	fun `Start Jay service while it is running`() {
		val mockedComponentName = mockk<ComponentName>()
		every { serviceInteractor.isJayServiceRunning() } returns true
		every { mockedContext.startForegroundService(any()) } returns mockedComponentName

		val result = serviceInteractor.startJayService()

		assertNull(result)
		verify(exactly = 1) { serviceInteractor.isJayServiceRunning() }
		verify(exactly = 0) { mockedContext.startForegroundService(any()) }
	}

	@Test
	fun `Stop Jay service while it is running`() {
		every { serviceInteractor.isJayServiceRunning() } returns true
		every { mockedContext.stopService(any()) } returns true

		val result = serviceInteractor.stopJayService()

		assertTrue(result)
		verify(exactly = 1) { serviceInteractor.isJayServiceRunning() }
		verify(exactly = 1) { mockedContext.stopService(any()) }
		verifySequence {
			serviceInteractor.stopJayService()
			serviceInteractor.isJayServiceRunning()
			mockedContext.stopService(any())
		}
	}

	@Test
	fun `Stop Jay service while it is not running`() {
		every { serviceInteractor.isJayServiceRunning() } returns false
		every { mockedContext.stopService(any()) } returns false

		val result = serviceInteractor.stopJayService()

		assertFalse(result)
		verify(exactly = 1) { serviceInteractor.isJayServiceRunning() }
		verify(exactly = 0) { mockedContext.stopService(any()) }
	}
}