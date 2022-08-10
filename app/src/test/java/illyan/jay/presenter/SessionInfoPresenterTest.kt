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
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.session_info.SessionInfoPresenter
import illyan.jay.ui.sessions.session_info.model.UiSession
import illyan.jay.util.TimeUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.util.*
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.*

class SessionInfoPresenterTest : TestBase() {

	private lateinit var mockedSessionInteractor: SessionInteractor
	private lateinit var sessionInfoPresenter: SessionInfoPresenter

	private val random = Random(Instant.now().nano)
	private val now = Instant.now()

	private val session = DomainSession(4, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 4.0)
	private val ongoingSession = DomainSession(5, Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), null, 5.0)
	private val uiSession = UiSession(session.id, session.startTime, session.endTime, session.distance)
	private val uiOngoingSession = UiSession(ongoingSession.id, ongoingSession.startTime, ongoingSession.endTime, ongoingSession.distance)

	private val sessions = listOf(
		DomainSession(1,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 1.0),
		DomainSession(2,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 2.0),
		DomainSession(3,  Date.from(Instant.ofEpochMilli(random.nextLong(now.toEpochMilli() - TimeUtils.DAY_IN_MILLIS, now.toEpochMilli()))), Date.from(now), 3.0),
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
		sessionInfoPresenter = SessionInfoPresenter(mockedSessionInteractor)
	}

	@ExperimentalCoroutinesApi
	@ParameterizedTest(name = "Session ID = {0}")
	@ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
	fun `Get session by id`(sessionId: Long) = runTest {
		every { mockedSessionInteractor.getSession(sessionId) } returns flowOf(sessions.firstOrNull { it.id == sessionId })

		var result: UiSession? = null
		sessionInfoPresenter.getSession(sessionId).collect { result = it }

		// Wait coroutine to collect the data.
		advanceUntilIdle()

		assertEquals(uiSessions.firstOrNull { it.id == sessionId }, result)
		verify(exactly = 1) { mockedSessionInteractor.getSession(sessionId) }
	}
}