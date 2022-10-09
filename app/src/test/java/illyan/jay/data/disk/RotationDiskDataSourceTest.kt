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
import illyan.jay.data.disk.dao.RotationDao
import illyan.jay.data.disk.datasource.RotationDiskDataSource
import illyan.jay.data.disk.model.RoomRotation
import illyan.jay.domain.model.DomainRotation
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
class RotationDiskDataSourceTest : TestBase() {

    private lateinit var mockedDao: RotationDao
    private lateinit var rotationDiskDataSource: RotationDiskDataSource

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val rotation = DomainRotation(
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
    private val roomRotation = RoomRotation(
        4,
        2,
        rotation.time.toInstant().toEpochMilli(),
        4,
        4f,
        4f,
        4f
    )
    private val rotations = listOf(
        DomainRotation(
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
        DomainRotation(
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
        DomainRotation(
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
        rotation
    )
    private val roomRotations = listOf(
        RoomRotation(1, 1, rotations[0].time.toInstant().toEpochMilli(), 1, 1f, 1f, 1f),
        RoomRotation(2, 1, rotations[1].time.toInstant().toEpochMilli(), 2, 2f, 2f, 2f),
        RoomRotation(3, 1, rotations[2].time.toInstant().toEpochMilli(), 3, 3f, 3f, 3f),
        roomRotation
    )

    @BeforeEach
    fun initEach() {
        mockedDao = mockk(relaxed = true)
        rotationDiskDataSource = RotationDiskDataSource(mockedDao)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get all rotations for a particular session`(sessionId: Long) = runTest {
        every { mockedDao.getRotations(sessionId) } returns flowOf(
            roomRotations.filter { it.sessionId == sessionId }
        )

        var result = listOf<DomainRotation>()
        rotationDiskDataSource.getRotations(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(rotations.filter { it.sessionId == sessionId }, result)
        verify(exactly = 1) { mockedDao.getRotations(sessionId) }
    }

    @Test
    fun `Save single rotation data`() {
        every { mockedDao.upsertRotation(rotation.toRoomModel()) } returns rotation.id

        val result = rotationDiskDataSource.saveRotation(rotation)

        assertEquals(rotation.id, result)
        verify(exactly = 1) { mockedDao.upsertRotation(rotation.toRoomModel()) }
    }

    @Test
    fun `Save multiple rotation data`() {
        rotationDiskDataSource.saveRotations(rotations)

        verify(exactly = 1) {
            mockedDao.upsertRotations(rotations.map(DomainRotation::toRoomModel))
        }
    }
}
