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

import com.google.android.gms.maps.model.LatLng
import illyan.jay.TestBase
import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.domain.interactor.LocationInteractor
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
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ExtendWith(MockKExtension::class)
class LocationInteractorTest : TestBase() {

    private lateinit var mockedLocationDiskDataSource: LocationDiskDataSource
    private lateinit var locationInteractor: LocationInteractor

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val location = DomainLocation(
        id = 4,
        sessionId = 2,
        zonedDateTime = Instant.ofEpochMilli(
            random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
        ).atZone(ZoneOffset.UTC),
        latLng = LatLng(4.0, 4.0),
        speed = 4f,
        accuracy = 4f,
        bearing = 4f,
        bearingAccuracy = 4f,
        altitude = 4.0,
        speedAccuracy = 4f,
        verticalAccuracy = 4f
    )
    private val locations = listOf(
        DomainLocation(
            id = 1,
            sessionId = 1,
            zonedDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            latLng = LatLng(1.0, 1.0),
            speed = 1f,
            accuracy = 1f,
            bearing = 1f,
            bearingAccuracy = 1f,
            altitude = 1.0,
            speedAccuracy = 1f,
            verticalAccuracy = 1f
        ),
        DomainLocation(
            id = 2,
            sessionId = 1,
            zonedDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            latLng = LatLng(2.0, 2.0),
            speed = 2f,
            accuracy = 2f,
            bearing = 2f,
            bearingAccuracy = 2f,
            altitude = 2.0,
            speedAccuracy = 2f,
            verticalAccuracy = 2f
        ),
        DomainLocation(
            id = 3,
            sessionId = 1,
            zonedDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            latLng = LatLng(3.0, 3.0),
            speed = 3f,
            accuracy = 3f,
            bearing = 3f,
            bearingAccuracy = 3f,
            altitude = 3.0,
            speedAccuracy = 3f,
            verticalAccuracy = 3f
        ), location
    )

    @BeforeEach
    fun initEach() {
        mockedLocationDiskDataSource = mockk(relaxed = true)
        locationInteractor = LocationInteractor(mockedLocationDiskDataSource)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get all locations for a particular session`(sessionId: Long) = runTest {
        every { mockedLocationDiskDataSource.getLocations(sessionId) } returns flowOf(
            locations.filter { it.sessionId == sessionId }
        )

        var result = listOf<DomainLocation>()
        locationInteractor.getLocations(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(locations.filter { it.sessionId == sessionId }, result)
    }

    @Test
    fun `Save single location data`() {
        every { mockedLocationDiskDataSource.saveLocation(location) } returns location.id

        val result = locationInteractor.saveLocation(location)

        assertEquals(location.id, result)
        verify(exactly = 1) { mockedLocationDiskDataSource.saveLocation(location) }
    }

    @Test
    fun `Save multiple location data`() {
        locationInteractor.saveLocations(locations)

        verify(exactly = 1) { mockedLocationDiskDataSource.saveLocations(locations) }
    }
}
