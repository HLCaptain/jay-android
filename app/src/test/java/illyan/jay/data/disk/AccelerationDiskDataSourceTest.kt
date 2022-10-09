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

package illyan.jay.data.disk

import illyan.jay.TestBase
import illyan.jay.data.disk.dao.AccelerationDao
import illyan.jay.data.disk.datasource.AccelerationDiskDataSource
import illyan.jay.data.disk.model.RoomAcceleration
import illyan.jay.domain.model.DomainAcceleration
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.util.Date
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ExtendWith(MockKExtension::class)
class AccelerationDiskDataSourceTest : TestBase() {

    private lateinit var mockedDao: AccelerationDao
    private lateinit var accelerationDiskDataSource: AccelerationDiskDataSource

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val acceleration = DomainAcceleration(
        4,
        2,
        Date.from(
            Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            )
        ),
        4,
        4f,
        4f,
        4f
    )
    private val roomAcceleration = RoomAcceleration(
        4,
        2,
        acceleration.time.toInstant().toEpochMilli(),
        4,
        4f,
        4f,
        4f
    )
    private val accelerations = listOf(
        DomainAcceleration(
            1,
            1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - 1.days.inWholeMilliseconds,
                        now.toEpochMilli()
                    )
                )
            ),
            1,
            1f,
            1f,
            1f
        ),
        DomainAcceleration(
            2,
            1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - 1.days.inWholeMilliseconds,
                        now.toEpochMilli()
                    )
                )
            ),
            2,
            2f,
            2f,
            2f
        ),
        DomainAcceleration(
            3,
            1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - 1.days.inWholeMilliseconds,
                        now.toEpochMilli()
                    )
                )
            ),
            3,
            3f,
            3f,
            3f
        ),
        acceleration
    )
    private val roomAccelerations = listOf(
        RoomAcceleration(1, 1, accelerations[0].time.toInstant().toEpochMilli(), 1, 1f, 1f, 1f),
        RoomAcceleration(2, 1, accelerations[1].time.toInstant().toEpochMilli(), 2, 2f, 2f, 2f),
        RoomAcceleration(3, 1, accelerations[2].time.toInstant().toEpochMilli(), 3, 3f, 3f, 3f),
        roomAcceleration
    )

    @BeforeEach
    fun initEach() {
        mockedDao = mockk(relaxed = true)
        accelerationDiskDataSource = AccelerationDiskDataSource(mockedDao)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get all accelerations for a particular session`(sessionId: Long) = runTest {
        every { mockedDao.getAccelerations(sessionId) } returns flowOf(
            roomAccelerations.filter { it.sessionId == sessionId }
        )

        var result = listOf<DomainAcceleration>()
        accelerationDiskDataSource.getAccelerations(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(accelerations.filter { it.sessionId == sessionId }, result)
        verify(exactly = 1) { mockedDao.getAccelerations(sessionId) }
    }

    @Test
    fun `Save single acceleration data`() {
        every { mockedDao.upsertAcceleration(acceleration.toRoomModel()) } returns acceleration.id

        val result = accelerationDiskDataSource.saveAcceleration(acceleration)

        assertEquals(acceleration.id, result)
        verify(exactly = 1) { mockedDao.upsertAcceleration(acceleration.toRoomModel()) }
    }

    @Test
    fun `Save multiple acceleration data`() {
        accelerationDiskDataSource.saveAccelerations(accelerations)

        verify(exactly = 1) {
            mockedDao.upsertAccelerations(
                accelerations.map(DomainAcceleration::toRoomModel)
            )
        }
    }
}
