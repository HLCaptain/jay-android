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

package illyan.jay.data.sensor

import illyan.jay.domain.model.AdvancedImuSensorData
import illyan.jay.domain.model.DomainSensorEvent
import org.apache.commons.math3.linear.DecompositionSolver
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import kotlin.math.pow

object SensorFusion {
    fun fuseSensors(
        accRaw: List<DomainSensorEvent>,
        accSmooth: List<DomainSensorEvent>,
        dirX: List<DomainSensorEvent>,
        dirY: List<DomainSensorEvent>,
        dirZ: List<DomainSensorEvent>,
        angVel: List<DomainSensorEvent>,
        angAccel: List<DomainSensorEvent>,
    ): List<AdvancedImuSensorData> {

        // Merge all timestamps
        val allTimestamps = (accRaw + accSmooth + dirX + dirY + dirZ + angVel + angAccel)
            .map { it.zonedDateTime.toInstant().toEpochMilli() }
            .distinct()
            .sorted() // 400 is the chunk size for the ML model

        return allTimestamps.map { timestamp ->
            // Interpolate values for each timestamp
            AdvancedImuSensorData(
                dirX = interpolateValue(dirX, timestamp),
                dirY = interpolateValue(dirY, timestamp),
                dirZ = interpolateValue(dirZ, timestamp),
                accRaw = interpolateValue(accRaw, timestamp),
                accSmooth = interpolateValue(accSmooth, timestamp),
                angVel = interpolateValue(angVel, timestamp),
                angAccel = interpolateValue(angAccel, timestamp),
                timestamp = timestamp
            )
        }
    }

    fun interpolateValue(events: List<DomainSensorEvent>, timestamp: Long): Triple<Double, Double, Double> {
        if (events.isEmpty()) return Triple(0.0, 0.0, 0.0)

        val degree = events.size / 10 // Degree of polynomial regression
        val xValues = events.map { it.zonedDateTime.toInstant().toEpochMilli().toDouble() }

        // Create design matrix
        val designMatrix = Array(events.size) { i -> DoubleArray(degree + 1) { j -> xValues[i].pow(j) } }

        // Calculating polynomial regression for each component (X, Y, Z)
        val xReg = polynomialRegression(designMatrix, events.map { it.x.toDouble() }, degree)
        val yReg = polynomialRegression(designMatrix, events.map { it.y.toDouble() }, degree)
        val zReg = polynomialRegression(designMatrix, events.map { it.z.toDouble() }, degree)

        return Triple(xReg(timestamp), yReg(timestamp), zReg(timestamp))
    }

    fun polynomialRegression(designMatrix: Array<DoubleArray>, yValues: List<Double>, degree: Int): (Long) -> Double {
        val yVector = MatrixUtils.createRealVector(yValues.toDoubleArray())
        val xMatrix = MatrixUtils.createRealMatrix(designMatrix)
        val solver: DecompositionSolver = QRDecomposition(xMatrix).solver
        val coefficients = solver.solve(yVector)

        return { x: Long -> (0..degree).toList().sumOf { i -> coefficients.getEntry(i) * x.toDouble().pow(i) } }
    }
}