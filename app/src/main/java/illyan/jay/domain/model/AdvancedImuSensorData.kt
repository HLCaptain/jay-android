/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

data class AdvancedImuSensorData(
    val dirX: Triple<Double, Double, Double>, // XYZ
    val dirY: Triple<Double, Double, Double>, // XYZ
    val dirZ: Triple<Double, Double, Double>, // XYZ
    val accRaw: Triple<Double, Double, Double>, // XYZ
    val accSmooth: Triple<Double, Double, Double>, // XYZ
    val angVel: Triple<Double, Double, Double>, // XYZ
    val angAccel: Triple<Double, Double, Double>, // XYZ
    val timestamp: Long, // Milliseconds
    val sessionUUID: Long = 0L,
)