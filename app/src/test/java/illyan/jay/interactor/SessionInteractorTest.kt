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

package illyan.jay.interactor

import illyan.jay.TestBase
import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.data.disk.datasource.SensorEventDiskDataSource
import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.domain.interactor.SearchInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainSession
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ExtendWith(MockKExtension::class)
class SessionInteractorTest : TestBase() {

    private lateinit var mockedSessionDiskDataSource: SessionDiskDataSource
    private lateinit var mockedSensorEventDiskDataSource: SensorEventDiskDataSource
    private lateinit var mockedLocationDiskDataSource: LocationDiskDataSource
    private lateinit var sessionInteractor: SessionInteractor
    private lateinit var mockedSearchInteractor: SearchInteractor
    private lateinit var mockedCoroutineScope: CoroutineScope

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val session = DomainSession(
        4,
        startDateTime = Instant.ofEpochMilli(
            random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
        ).atZone(ZoneOffset.UTC),
        endDateTime = now.atZone(ZoneOffset.UTC),
    )
    private val ongoingSession = DomainSession(
        5,
        startDateTime = Instant.ofEpochMilli(
            random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
        ).atZone(ZoneOffset.UTC),
        null,
    )
    private val sessions = listOf(
        DomainSession(
            1,
            startDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            endDateTime = now.atZone(ZoneOffset.UTC),
        ),
        DomainSession(
            2,
            startDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            endDateTime = now.atZone(ZoneOffset.UTC),
        ),
        DomainSession(
            3,
            startDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            endDateTime = now.atZone(ZoneOffset.UTC),
        ),
        session,
        ongoingSession
    )

    @BeforeEach
    fun initEach() {
        mockedSessionDiskDataSource = mockk(relaxed = true)
        mockedSensorEventDiskDataSource = mockk(relaxed = true)
        mockedLocationDiskDataSource = mockk(relaxed = true)
        mockedSearchInteractor = mockk(relaxed = true)
        mockedCoroutineScope = mockk(relaxed = true)

        sessionInteractor = spyk(
            SessionInteractor(
                mockedSessionDiskDataSource,
                mockedSensorEventDiskDataSource,
                mockedLocationDiskDataSource,
                mockedSearchInteractor
            )
        )
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get session by id`(sessionId: Long) = runTest {
        every { mockedSessionDiskDataSource.getSession(sessionId) } returns flowOf(
            sessions.firstOrNull { it.id == sessionId }
        )

        var result: DomainSession? = null
        sessionInteractor.getSession(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(sessions.firstOrNull { it.id == sessionId }, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.getSession(sessionId) }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Get sessions`() = runTest {
        every { mockedSessionDiskDataSource.getSessions() } returns flowOf(sessions)

        var result = listOf<DomainSession>()
        sessionInteractor.getSessions().collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(sessions, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.getSessions() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Get ongoing sessions`() = runTest {
        every { mockedSessionDiskDataSource.getOngoingSessions() } returns flowOf(
            sessions.filter { it.endDateTime == null }
        )

        var result = listOf<DomainSession>()
        sessionInteractor.getOngoingSessions().collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(sessions.filter { it.endDateTime == null }, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.getOngoingSessions() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Get ongoing session ids`() = runTest {
        every { mockedSessionDiskDataSource.getOngoingSessionIds() } returns flowOf(
            sessions.filter { it.endDateTime == null }
                .map { it.id }
        )

        var result = listOf<Long>()
        sessionInteractor.getOngoingSessionIds().collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(sessions.filter { it.endDateTime == null }.map { it.id }, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.getOngoingSessionIds() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Start a session`() = runTest {
        every { mockedSessionDiskDataSource.startSession() } returns sessions.size + 1L

        val result = sessionInteractor.startSession(mockedCoroutineScope)

        assertEquals(sessions.size + 1L, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.startSession() }
    }

    @Test
    fun `Stop a session`() {
        every { mockedSessionDiskDataSource.stopSession(ongoingSession) } returns ongoingSession.id

        val result = sessionInteractor.stopSession(ongoingSession)

        assertEquals(ongoingSession.id, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.stopSession(ongoingSession) }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Stop ongoing sessions`() = runTest {
        val ongoingSession = ongoingSession.copy()
        every { sessionInteractor.getOngoingSessions() } returns flowOf(listOf(ongoingSession))

        launch { sessionInteractor.stopOngoingSessions(mockedCoroutineScope) }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        verify(exactly = 1) { sessionInteractor.getOngoingSessions() }
        verify(exactly = 1) { mockedSessionDiskDataSource.stopSessions(listOf(ongoingSession)) }
        verifyOrder {
            launch { sessionInteractor.stopOngoingSessions(any()) }
            sessionInteractor.getOngoingSessions()
            mockedSessionDiskDataSource.stopSessions(listOf(ongoingSession))
        }
    }

    @Test
    fun `Save single session data`() {
        every { mockedSessionDiskDataSource.saveSession(session) } returns session.id

        val result = sessionInteractor.saveSession(session)

        assertEquals(session.id, result)
        verify(exactly = 1) { mockedSessionDiskDataSource.saveSession(session) }
    }

    @Test
    fun `Save multiple session data`() {
        sessionInteractor.saveSessions(sessions)

        verify(exactly = 1) { mockedSessionDiskDataSource.saveSessions(sessions) }
    }
}
