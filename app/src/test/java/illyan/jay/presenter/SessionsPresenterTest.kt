/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.presenter

import com.google.android.gms.maps.model.LatLng
import illyan.jay.TestBase
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.list.SessionsPresenter
import illyan.jay.ui.sessions.list.model.UiLocation
import illyan.jay.ui.sessions.list.model.UiSession
import illyan.jay.util.TimeUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.*
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SessionsPresenterTest : TestBase() {

    private lateinit var mockedSessionInteractor: SessionInteractor
    private lateinit var mockedLocationInteractor: LocationInteractor
    private lateinit var sessionsPresenter: SessionsPresenter

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val location = DomainLocation(
        4, LatLng(4.0, 4.0), 4f, 2,
        Date.from(
            Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli())
            )
        ),
        4f, 4f, 4f, 4.0, 4f, 4f
    )
    private val uiLocation = UiLocation(
        location.id,
        location.latLng,
        location.speed,
        location.sessionId,
        location.time,
        location.accuracy,
        location.bearing,
        location.bearingAccuracy,
        location.altitude,
        location.speedAccuracy,
        location.verticalAccuracy
    )

    private val locations = listOf(
        DomainLocation(
            1, LatLng(1.0, 1.0), 1f, 1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS,
                        now.toEpochMilli()
                    )
                )
            ),
            1f, 1f, 1f, 1.0, 1f, 1f
        ),
        DomainLocation(
            2, LatLng(2.0, 2.0), 2f, 2,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS,
                        now.toEpochMilli()
                    )
                )
            ),
            2f, 2f, 2f, 2.0, 2f, 2f
        ),
        DomainLocation(
            3, LatLng(3.0, 3.0), 3f, 3,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS,
                        now.toEpochMilli()
                    )
                )
            ),
            3f, 3f, 3f, 3.0, 3f, 3f
        ),
        location
    )
    private val uiLocations = listOf(
        UiLocation(
            locations[0].id,
            locations[0].latLng,
            locations[0].speed,
            locations[0].sessionId,
            locations[0].time,
            locations[0].accuracy,
            locations[0].bearing,
            locations[0].bearingAccuracy,
            locations[0].altitude,
            locations[0].speedAccuracy,
            locations[0].verticalAccuracy
        ),
        UiLocation(
            locations[1].id,
            locations[1].latLng,
            locations[1].speed,
            locations[1].sessionId,
            locations[1].time,
            locations[1].accuracy,
            locations[1].bearing,
            locations[1].bearingAccuracy,
            locations[1].altitude,
            locations[1].speedAccuracy,
            locations[1].verticalAccuracy
        ),
        UiLocation(
            locations[2].id,
            locations[2].latLng,
            locations[2].speed,
            locations[2].sessionId,
            locations[2].time,
            locations[2].accuracy,
            locations[2].bearing,
            locations[2].bearingAccuracy,
            locations[2].altitude,
            locations[2].speedAccuracy,
            locations[2].verticalAccuracy
        ),
        uiLocation
    )

    private val session = DomainSession(
        4,
        Date.from(
            Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli())
            )
        ),
        Date.from(now),
        4.0
    )
    private val ongoingSession = DomainSession(
        5,
        Date.from(
            Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli())
            )
        ),
        null,
        5.0
    )
    private val uiSession = UiSession(
        session.id,
        session.startTime,
        session.endTime,
        session.distance
    )
    private val uiOngoingSession = UiSession(
        ongoingSession.id,
        ongoingSession.startTime,
        ongoingSession.endTime,
        ongoingSession.distance
    )

    private val sessions = listOf(
        DomainSession(
            1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS,
                        now.toEpochMilli()
                    )
                )
            ),
            Date.from(now),
            1.0
        ),
        DomainSession(
            2,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS,
                        now.toEpochMilli()
                    )
                )
            ),
            Date.from(now),
            2.0
        ),
        DomainSession(
            3,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS,
                        now.toEpochMilli()
                    )
                )
            ),
            Date.from(now),
            3.0
        ),
        session,
        ongoingSession
    )
    private val uiSessions = listOf(
        UiSession(sessions[0].id, sessions[0].startTime, sessions[0].endTime, sessions[0].distance),
        UiSession(sessions[1].id, sessions[1].startTime, sessions[1].endTime, sessions[1].distance),
        UiSession(sessions[2].id, sessions[2].startTime, sessions[2].endTime, sessions[2].distance),
        uiSession,
        uiOngoingSession
    )

    @BeforeEach
    fun initEach() {
        mockedSessionInteractor = mockk(relaxed = true)
        mockedLocationInteractor = mockk(relaxed = true)
        sessionsPresenter = SessionsPresenter(mockedSessionInteractor, mockedLocationInteractor)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get locations for a particular session`(sessionId: Long) = runTest {
        every { mockedLocationInteractor.getLocations(sessionId) } returns flowOf(
            locations.filter { it.sessionId == sessionId }
        )

        var result = listOf<UiLocation>()
        sessionsPresenter.getLocations(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(uiLocations.filter { it.sessionId == sessionId }, result)
        verify(exactly = 1) { mockedLocationInteractor.getLocations(sessionId) }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Get sessions`() = runTest {
        every { mockedSessionInteractor.getSessions() } returns flowOf(sessions)

        var result = listOf<UiSession>()
        sessionsPresenter.getSessions().collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(uiSessions, result)
        verify(exactly = 1) { mockedSessionInteractor.getSessions() }
    }
}
