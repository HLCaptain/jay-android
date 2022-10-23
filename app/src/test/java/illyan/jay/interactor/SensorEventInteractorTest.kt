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

import android.hardware.Sensor
import illyan.jay.TestBase
import illyan.jay.data.disk.datasource.SensorEventDiskDataSource
import illyan.jay.domain.interactor.SensorEventInteractor
import illyan.jay.domain.model.DomainSensorEvent
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ExtendWith(MockKExtension::class)
class SensorEventInteractorTest : TestBase() {

    private lateinit var mockedSensorEventDiskDataSource: SensorEventDiskDataSource
    private lateinit var sensorEventInteractor: SensorEventInteractor

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

    @BeforeEach
    fun initEach() {
        mockedSensorEventDiskDataSource = mockk(relaxed = true)
        sensorEventInteractor = SensorEventInteractor(mockedSensorEventDiskDataSource)
    }

    @Test
    fun `Save single acceleration data`() {
        every {
            mockedSensorEventDiskDataSource.saveSensorEvent(acceleration)
        } returns acceleration.id

        val result = sensorEventInteractor.saveSensorEvent(acceleration)

        assertEquals(acceleration.id, result)
        verify(exactly = 1) { mockedSensorEventDiskDataSource.saveSensorEvent(acceleration) }
    }

    @Test
    fun `Save multiple acceleration data`() {
        sensorEventInteractor.saveSensorEvents(accelerations)

        verify(exactly = 1) { mockedSensorEventDiskDataSource.saveSensorEvents(accelerations) }
    }
}
