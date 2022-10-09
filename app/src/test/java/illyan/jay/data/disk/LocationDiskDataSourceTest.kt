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

import com.google.android.gms.maps.model.LatLng
import illyan.jay.TestBase
import illyan.jay.data.disk.dao.LocationDao
import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.domain.model.DomainLocation
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
class LocationDiskDataSourceTest : TestBase() {

    private lateinit var mockedDao: LocationDao
    private lateinit var locationDiskDataSource: LocationDiskDataSource

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val location = DomainLocation(
        4, LatLng(4.0, 4.0), 4f, 2,
        Date.from(
            Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            )
        ),
        4f, 4f, 4f, 4.0, 4f, 4f
    )
    private val roomLocation = RoomLocation(
        4,
        2,
        4.0,
        4.0,
        4f,
        location.time.toInstant().toEpochMilli(),
        4f,
        4f,
        4f,
        4f,
        4.0,
        4f
    )
    private val locations = listOf(
        DomainLocation(
            1, LatLng(1.0, 1.0), 1f, 1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - 1.days.inWholeMilliseconds,
                        now.toEpochMilli()
                    )
                )
            ),
            1f, 1f, 1f, 1.0, 1f, 1f
        ),
        DomainLocation(
            2, LatLng(2.0, 2.0), 2f, 1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - 1.days.inWholeMilliseconds,
                        now.toEpochMilli()
                    )
                )
            ),
            2f, 2f, 2f, 2.0, 2f, 2f
        ),
        DomainLocation(
            3, LatLng(3.0, 3.0), 3f, 1,
            Date.from(
                Instant.ofEpochMilli(
                    random.nextLong(
                        now.toEpochMilli() - 1.days.inWholeMilliseconds,
                        now.toEpochMilli()
                    )
                )
            ),
            3f, 3f, 3f, 3.0, 3f, 3f
        ),
        location
    )
    private val roomLocations = listOf(
        RoomLocation(
            1,
            1,
            1.0,
            1.0,
            1f,
            locations[0].time.toInstant().toEpochMilli(),
            1f,
            1f,
            1f,
            1f,
            1.0,
            1f
        ),
        RoomLocation(
            2,
            1,
            2.0,
            2.0,
            2f,
            locations[1].time.toInstant().toEpochMilli(),
            2f,
            2f,
            2f,
            2f,
            2.0,
            2f
        ),
        RoomLocation(
            3,
            1,
            3.0,
            3.0,
            3f,
            locations[2].time.toInstant().toEpochMilli(),
            3f,
            3f,
            3f,
            3f,
            3.0,
            3f
        ),
        roomLocation
    )

    @BeforeEach
    fun initEach() {
        mockedDao = mockk(relaxed = true)
        locationDiskDataSource = LocationDiskDataSource(mockedDao)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get all locations for a particular session`(sessionId: Long) = runTest {
        every { mockedDao.getLocations(sessionId) } returns flowOf(
            roomLocations.filter { it.sessionId == sessionId }
        )

        var result = listOf<DomainLocation>()
        locationDiskDataSource.getLocations(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(locations.filter { it.sessionId == sessionId }, result)
        verify(exactly = 1) { mockedDao.getLocations(sessionId) }
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get latest locations for a particular session with a limit`(sessionId: Long) = runTest {
        val limit = 2L
        val sortedRoomLocations = roomLocations.toList().sortedByDescending { it.time }
        val sortedLocations = locations.toList().sortedByDescending { it.time }
        every { mockedDao.getLatestLocations(sessionId, limit) } returns flowOf(
            sortedRoomLocations.filter { it.sessionId == sessionId }.take(limit.toInt())
        )

        var result = listOf<DomainLocation>()
        locationDiskDataSource.getLatestLocations(sessionId, limit).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(
            sortedLocations.filter { it.sessionId == sessionId }.take(limit.toInt()),
            result
        )
        verify(exactly = 1) { mockedDao.getLatestLocations(sessionId, limit) }
    }

    @Test
    fun `Save single location data`() {
        every { mockedDao.upsertLocation(location.toRoomModel()) } returns location.id

        val result = locationDiskDataSource.saveLocation(location)

        assertEquals(location.id, result)
        verify(exactly = 1) { mockedDao.upsertLocation(location.toRoomModel()) }
    }

    @Test
    fun `Save multiple location data`() {
        locationDiskDataSource.saveLocations(locations)

        verify(exactly = 1) {
            mockedDao.upsertLocations(locations.map(DomainLocation::toRoomModel))
        }
    }
}
