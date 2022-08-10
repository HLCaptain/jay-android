/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.presenter

import com.google.android.gms.maps.model.LatLng
import illyan.jay.TestBase
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.ui.sessions.map.SessionMapPresenter
import illyan.jay.ui.sessions.map.model.UiLocation
import illyan.jay.util.Color
import illyan.jay.util.TimeUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.util.*
import kotlin.random.Random

class SessionMapPresenterTest : TestBase() {

	private lateinit var mockedLocationInteractor: LocationInteractor
	private lateinit var sessionMapPresenter: SessionMapPresenter

	private val random = Random(Instant.now().nano)
	private val now = Instant.now()

	private val location = DomainLocation(4, LatLng(4.0, 4.0), 4f, 2, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), 4f, 4f, 4f, 4.0, 4f, 4f)
	private val uiLocation = UiLocation(location.id, location.latLng, location.sessionId, location.time, Color.MAGENTA)

	private val locations = listOf(
		DomainLocation(1, LatLng(1.0, 1.0), 1f, 1,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), 1f, 1f, 1f, 1.0, 1f, 1f),
		DomainLocation(2, LatLng(2.0, 2.0), 2f, 2, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), 2f, 2f, 2f, 2.0, 2f, 2f),
		DomainLocation(3, LatLng(3.0, 3.0), 3f, 3, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), 3f, 3f, 3f, 3.0, 3f, 3f),
		location,
	)
	private val uiLocations = listOf(
		UiLocation(locations[0].id, locations[0].latLng, locations[0].sessionId, locations[0].time, Color.MAGENTA),
		UiLocation(locations[1].id, locations[1].latLng, locations[1].sessionId, locations[1].time, Color.MAGENTA),
		UiLocation(locations[2].id, locations[2].latLng, locations[2].sessionId, locations[2].time, Color.MAGENTA),
		uiLocation,
	)

	@BeforeEach
	fun initEach() {
		mockedLocationInteractor = mockk(relaxed = true)
		sessionMapPresenter = SessionMapPresenter(mockedLocationInteractor)
	}

	@ExperimentalCoroutinesApi
	@ParameterizedTest(name = "Session ID = {0}")
	@ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
	fun `Get locations for a particular session`(sessionId: Long) = runTest {
		every { mockedLocationInteractor.getLocations(sessionId) } returns flowOf(locations.filter { it.sessionId == sessionId })

		var result = listOf<UiLocation>()
		sessionMapPresenter.getLocations(sessionId).collect { result = it }

		// Wait coroutine to collect the data.
		advanceUntilIdle()

		assertEquals(uiLocations.filter { it.sessionId == sessionId }, result)
		verify(exactly = 1) { mockedLocationInteractor.getLocations(sessionId) }
	}
}