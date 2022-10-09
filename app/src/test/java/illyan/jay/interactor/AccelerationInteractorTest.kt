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
import illyan.jay.data.disk.datasource.AccelerationDiskDataSource
import illyan.jay.domain.interactor.AccelerationInteractor
import illyan.jay.domain.model.DomainAcceleration
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.Date
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ExtendWith(MockKExtension::class)
class AccelerationInteractorTest : TestBase() {

    private lateinit var mockedAccelerationDiskDataSource: AccelerationDiskDataSource
    private lateinit var accelerationInteractor: AccelerationInteractor

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

    @BeforeEach
    fun initEach() {
        mockedAccelerationDiskDataSource = mockk(relaxed = true)
        accelerationInteractor = AccelerationInteractor(mockedAccelerationDiskDataSource)
    }

    @Test
    fun `Save single acceleration data`() {
        every {
            mockedAccelerationDiskDataSource.saveAcceleration(acceleration)
        } returns acceleration.id

        val result = accelerationInteractor.saveAcceleration(acceleration)

        assertEquals(acceleration.id, result)
        verify(exactly = 1) { mockedAccelerationDiskDataSource.saveAcceleration(acceleration) }
    }

    @Test
    fun `Save multiple acceleration data`() {
        accelerationInteractor.saveAccelerations(accelerations)

        verify(exactly = 1) { mockedAccelerationDiskDataSource.saveAccelerations(accelerations) }
    }
}
