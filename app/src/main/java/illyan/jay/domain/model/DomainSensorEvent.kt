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

package illyan.jay.domain.model

import java.time.ZonedDateTime

/**
 * Domain acceleration used for general data handling
 * between DataSources, Interactors and Presenters.
 *
 * @property id
 * @property sessionUUID
 * @property zonedDateTime
 * @property accuracy
 * @property x
 * @property y
 * @property z
 * @constructor Create empty Domain acceleration
 */
data class DomainSensorEvent(
    val id: Long = -1,
    val sessionUUID: String,
    val zonedDateTime: ZonedDateTime,
    val accuracy: Byte, // enum
    val x: Float,
    val y: Float,
    val z: Float,
    val type: Byte
)
