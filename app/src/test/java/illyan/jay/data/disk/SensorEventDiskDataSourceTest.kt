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

import android.hardware.Sensor
import illyan.jay.TestBase
import illyan.jay.data.disk.dao.SensorEventDao
import illyan.jay.data.disk.datasource.SensorEventDiskDataSource
import illyan.jay.data.disk.model.RoomSensorEvent
import illyan.jay.domain.model.DomainSensorEvent
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
class SensorEventDiskDataSourceTest : TestBase() {

    private lateinit var mockedDao: SensorEventDao
    private lateinit var sensorEventDiskDataSource: SensorEventDiskDataSource

    private val random = Random(Instant.now().nano)
    private val now = Instant.now()

    private val acceleration = DomainSensorEvent(
        4,
        2,
        zonedDateTime = Instant.ofEpochMilli(
            random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
        ).atZone(ZoneOffset.UTC),
        4,
        4f,
        4f,
        4f,
        type = Sensor.STRING_TYPE_ACCELEROMETER
    )
    private val roomAcceleration = RoomSensorEvent(
        4,
        2,
        acceleration.zonedDateTime.toInstant().toEpochMilli(),
        4,
        4f,
        4f,
        4f,
        type = Sensor.STRING_TYPE_ACCELEROMETER
    )
    private val accelerations = listOf(
        DomainSensorEvent(
            1,
            1,
            zonedDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            1,
            1f,
            1f,
            1f,
            type = Sensor.STRING_TYPE_ACCELEROMETER
        ),
        DomainSensorEvent(
            2,
            1,
            zonedDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            2,
            2f,
            2f,
            2f,
            type = Sensor.STRING_TYPE_ACCELEROMETER
        ),
        DomainSensorEvent(
            3,
            1,
            zonedDateTime = Instant.ofEpochMilli(
                random.nextLong(now.toEpochMilli() - 1.days.inWholeMilliseconds, now.toEpochMilli())
            ).atZone(ZoneOffset.UTC),
            3,
            3f,
            3f,
            3f,
            type = Sensor.STRING_TYPE_ACCELEROMETER
        ),
        acceleration
    )
    private val roomAccelerations = listOf(
        RoomSensorEvent(1, 1, accelerations[0].zonedDateTime.toInstant().toEpochMilli(), 1, 1f, 1f, 1f,
            type = Sensor.STRING_TYPE_ACCELEROMETER),
        RoomSensorEvent(2, 1, accelerations[1].zonedDateTime.toInstant().toEpochMilli(), 2, 2f, 2f, 2f,
            type = Sensor.STRING_TYPE_ACCELEROMETER),
        RoomSensorEvent(3, 1, accelerations[2].zonedDateTime.toInstant().toEpochMilli(), 3, 3f, 3f, 3f,
            type = Sensor.STRING_TYPE_ACCELEROMETER),
        roomAcceleration
    )

    @BeforeEach
    fun initEach() {
        mockedDao = mockk(relaxed = true)
        sensorEventDiskDataSource = SensorEventDiskDataSource(mockedDao)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest(name = "Session ID = {0}")
    @ValueSource(longs = [1L, 2L, 3L, 4L, 5L, 6L, 0L, -1L])
    fun `Get all accelerations for a particular session`(sessionId: Long) = runTest {
        every { mockedDao.getSensorEvents(sessionId) } returns flowOf(
            roomAccelerations.filter { it.sessionId == sessionId }
        )

        var result = listOf<DomainSensorEvent>()
        sensorEventDiskDataSource.getSensorEvents(sessionId).collect { result = it }

        // Wait coroutine to collect the data.
        advanceUntilIdle()

        assertEquals(accelerations.filter { it.sessionUUID == sessionId }, result)
        verify(exactly = 1) { mockedDao.getSensorEvents(sessionId) }
    }

    @Test
    fun `Save single acceleration data`() {
        every { mockedDao.upsertSensorEvent(acceleration.toRoomModel()) } returns acceleration.id

        val result = sensorEventDiskDataSource.saveSensorEvent(acceleration)

        assertEquals(acceleration.id, result)
        verify(exactly = 1) { mockedDao.upsertSensorEvent(acceleration.toRoomModel()) }
    }

    @Test
    fun `Save multiple acceleration data`() {
        sensorEventDiskDataSource.saveSensorEvents(accelerations)

        verify(exactly = 1) {
            mockedDao.upsertSensorEvents(
                accelerations.map(DomainSensorEvent::toRoomModel)
            )
        }
    }
}
