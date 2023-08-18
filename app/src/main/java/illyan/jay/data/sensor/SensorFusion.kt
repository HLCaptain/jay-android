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
import timber.log.Timber
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
        Timber.d("Fusing sensor data")
        // Merge all timestamps
        val allTimestamps = (accRaw + accSmooth + dirX + dirY + dirZ + angVel + angAccel)
            .map { it.zonedDateTime.toInstant().toEpochMilli() }
            .distinct()
            .sorted() // 400 is the chunk size for the ML model

        if (allTimestamps.isEmpty()) Timber.d("No sensor data to fuse")

        val interpolatedDirX = interpolateValues(dirX, allTimestamps)
        val interpolatedDirY = interpolateValues(dirY, allTimestamps)
        val interpolatedDirZ = interpolateValues(dirZ, allTimestamps)
        val interpolatedAccRaw = interpolateValues(accRaw, allTimestamps)
        val interpolatedAccSmooth = interpolateValues(accSmooth, allTimestamps)
        val interpolatedAngVel = interpolateValues(angVel, allTimestamps)
        val interpolatedAngAccel = interpolateValues(angAccel, allTimestamps)

        return allTimestamps.mapIndexed { index, timestamp ->
            Timber.v("Fusing sensor data for timestamp $timestamp (${index + 1}/${allTimestamps.size})")
            // Interpolate values for each timestamp
            AdvancedImuSensorData(
                dirX = interpolatedDirX[index],
                dirY = interpolatedDirY[index],
                dirZ = interpolatedDirZ[index],
                accRaw = interpolatedAccRaw[index],
                accSmooth = interpolatedAccSmooth[index],
                angVel = interpolatedAngVel[index],
                angAccel = interpolatedAngAccel[index],
                timestamp = timestamp
            )
        }
    }

    private fun interpolateValues(events: List<DomainSensorEvent>, timestamps: List<Long>): List<Triple<Double, Double, Double>> {
        Timber.v("Interpolating values for ${timestamps.size} timestamp")
        val degree = events.size // Degree of polynomial regression
        val xValues = events.map { it.zonedDateTime.toInstant().toEpochMilli().toDouble() }

        // Create design matrix
        val designMatrix = Array(events.size) { i -> DoubleArray(degree + 1) { j -> xValues[i].pow(j) } }

        // Calculating polynomial regression for each component (X, Y, Z)
        val xReg = polynomialRegression(designMatrix, events.map { it.x.toDouble() }, degree)
        val yReg = polynomialRegression(designMatrix, events.map { it.y.toDouble() }, degree)
        val zReg = polynomialRegression(designMatrix, events.map { it.z.toDouble() }, degree)

        return timestamps.map { timestamp ->
            Triple(xReg(timestamp), yReg(timestamp), zReg(timestamp))
        }
    }

    private fun polynomialRegression(designMatrix: Array<DoubleArray>, yValues: List<Double>, degree: Int): (Long) -> Double {
//        Timber.v("Calculating polynomial regression for degree $degree")
        val yVector = MatrixUtils.createRealVector(yValues.toDoubleArray())
        if (designMatrix.isEmpty()) return { 0.0 }
        val xMatrix = MatrixUtils.createRealMatrix(designMatrix)
        val solver: DecompositionSolver = QRDecomposition(xMatrix).solver
        val coefficients = solver.solve(yVector)

        return { x: Long -> (0..degree).toList().sumOf { i -> coefficients.getEntry(i) * x.toDouble().pow(i) } }
    }
}