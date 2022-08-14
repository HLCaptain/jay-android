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

package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

/**
 * Simple sensor event listener.
 * You are free to set each callback functions.
 *
 * @property onSensorChangedCallback this method is invoked upon calling onSensorChanged.
 * @property onAccuracyChangedCallback this method is invoked upon calling onAccuracyCanged.
 * @constructor Create empty Simple sensor event listener
 */
class SimpleSensorEventListener(
    private var onSensorChangedCallback: (event: SensorEvent?) -> Unit = { _: SensorEvent? -> },
    private val onAccuracyChangedCallback: (sensor: Sensor?, accuracy: Int) -> Unit = { _: Sensor?, _: Int -> }
) : SensorEventListener {

    override fun onSensorChanged(event: SensorEvent?) {
        onSensorChangedCallback.invoke(event)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        onAccuracyChangedCallback.invoke(sensor, accuracy)
    }
}
