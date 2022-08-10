/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.disk

import illyan.jay.TestBase
import illyan.jay.data.disk.dao.SessionDao
import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.data.disk.model.RoomSession
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.TimeUtils
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.util.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class SessionDiskDataSourceTest : TestBase() {

	private lateinit var mockedDao: SessionDao
	private lateinit var sessionDiskDataSource: SessionDiskDataSource

	private val random = Random(Instant.now().nano)
	private val now = Instant.now()

	private val session = DomainSession(4, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 4.0)
	private val ongoingSession = DomainSession(5, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), null, 5.0)
	private val roomSession = RoomSession(4, session.startTime.toInstant().toEpochMilli(), now.toEpochMilli(), 4.0)
	private val roomOngoingSession = RoomSession(5, ongoingSession.startTime.toInstant().toEpochMilli(), null, 5.0)

	private val sessions = listOf(
		DomainSession(1,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 1.0),
		DomainSession(2,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 2.0),
		DomainSession(3,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 3.0),
		session,
		ongoingSession
	)
	private val roomSessions = listOf(
		RoomSession(1, sessions[0].startTime.toInstant().toEpochMilli(), now.toEpochMilli(), 1.0),
		RoomSession(2, sessions[1].startTime.toInstant().toEpochMilli(), now.toEpochMilli(), 2.0),
		RoomSession(3, sessions[2].startTime.toInstant().toEpochMilli(), now.toEpochMilli(), 3.0),
		roomSession,
		roomOngoingSession
	)

	@BeforeEach
	fun initEach() {
		mockedDao = mockk(relaxed = true)
		sessionDiskDataSource = SessionDiskDataSource(mockedDao)
	}

	@ExperimentalCoroutinesApi
	@ParameterizedTest(name = "Session ID = {0}")
	@ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
	fun `Get session by id`(sessionId: Long) = runTest {
		every { mockedDao.getSession(sessionId) } returns flowOf(roomSessions.firstOrNull { it.id == sessionId })

		var result: DomainSession? = null
		sessionDiskDataSource.getSession(sessionId).collect { result = it }

		// Wait coroutine to collect the data.
		advanceUntilIdle()

		assertEquals(sessions.firstOrNull { it.id == sessionId }, result)
		verify(exactly = 1) { mockedDao.getSession(sessionId) }
	}

	@ExperimentalCoroutinesApi
	@Test
	fun `Get sessions`() = runTest {
		every { mockedDao.getSessions() } returns flowOf(roomSessions)

		var result = listOf<DomainSession>()
		sessionDiskDataSource.getSessions().collect { result = it }

		// Wait coroutine to collect the data.
		advanceUntilIdle()

		assertEquals(sessions, result)
		verify(exactly = 1) { mockedDao.getSessions() }
	}

	@ExperimentalCoroutinesApi
	@Test
	fun `Get ongoing sessions`() = runTest {
		every { mockedDao.getOngoingSessions() } returns flowOf(roomSessions.filter { it.endTime == null })

		var result = listOf<DomainSession>()
		sessionDiskDataSource.getOngoingSessions().collect { result = it }

		// Wait coroutine to collect the data.
		advanceUntilIdle()

		assertEquals(sessions.filter { it.endTime == null }, result)
		verify(exactly = 1) { mockedDao.getOngoingSessions() }
	}

	@ExperimentalCoroutinesApi
	@Test
	fun `Get ongoing session ids`() = runTest {
		every { mockedDao.getOngoingSessionIds() } returns flowOf(roomSessions.filter { it.endTime == null }.map { it.id })

		var result = listOf<Long>()
		sessionDiskDataSource.getOngoingSessionIds().collect { result = it }

		// Wait coroutine to collect the data.
		advanceUntilIdle()

		assertEquals(sessions.filter { it.endTime == null }.map { it.id }, result)
		verify(exactly = 1) { mockedDao.getOngoingSessionIds() }
	}

	@Test
	fun `Start a session`() {
		every { mockedDao.insertSession(any()) } returns sessions.size + 1L

		val result = sessionDiskDataSource.startSession()

		assertEquals(sessions.size + 1L, result)
		verify(exactly = 1) { mockedDao.insertSession(any()) }
	}

	@Test
	fun `Stop a session`() {
		every { mockedDao.upsertSession(any()) } returns ongoingSession.id

		val result = sessionDiskDataSource.stopSession(ongoingSession)

		assertEquals(ongoingSession.id, result)
		verify(exactly = 1) { mockedDao.upsertSession(any()) }
	}

	@Test
	fun `Stop sessions`() {
		val ongoingSession = ongoingSession.copy()
		val session = session
		sessionDiskDataSource.stopSessions(listOf(ongoingSession, session))

		assertNotNull(ongoingSession.endTime)
		assertEquals(this.session.endTime, session.endTime)
		verify(exactly = 1) { mockedDao.upsertSessions(any()) }
	}

	@Test
	fun `Save single session data`() {
		every { mockedDao.upsertSession(session.toRoomModel()) } returns session.id

		val result = sessionDiskDataSource.saveSession(session)

		assertEquals(session.id, result)
		verify(exactly = 1) { mockedDao.upsertSession(session.toRoomModel()) }
	}

	@Test
	fun `Save multiple session data`() {
		sessionDiskDataSource.saveSessions(sessions)

		verify(exactly = 1) { mockedDao.upsertSessions(sessions.map(DomainSession::toRoomModel)) }
	}
}