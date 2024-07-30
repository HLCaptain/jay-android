/*
 * Copyright (c) 2023-2024 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.domain.interactor

import android.hardware.Sensor
import android.os.Build
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import illyan.jay.data.firebaseml.datasource.FirebaseMLDataSource
import illyan.jay.data.sensor.SensorFusion
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.util.toZonedDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.tensorflow.lite.Interpreter
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelInteractor @Inject constructor(
    private val firebaseMLDataSource: FirebaseMLDataSource,
    private val sensorEventInteractor: SensorEventInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) {
    val downloadingModels = firebaseMLDataSource.downloadingModels
    val availableModels = firebaseMLDataSource.availableModels
    val downloadedModels = firebaseMLDataSource.downloadedModels

    fun getModel(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LATEST_MODEL,
    ) = firebaseMLDataSource.getModel(modelName, conditions, downloadType)

    fun getModelId(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LATEST_MODEL,
    ) = firebaseMLDataSource.getModelId(modelName, conditions, downloadType)

    fun getDownloadedModels() = firebaseMLDataSource.getDownloadedModels()

    fun refreshAvailableModelsList() = firebaseMLDataSource.refreshAvailableModelsList()

    suspend fun deleteAllModels() = firebaseMLDataSource.deleteAllModels()

    fun deleteModel(modelName: String) = firebaseMLDataSource.deleteModel(modelName)

    suspend fun getFilteredDriverAggression(
        modelName: String,
        sessionUUID: String
    ): Flow<Map<ZonedDateTime, Double>> {
        Timber.d("Filtering aggression values for session ${sessionUUID.take(4)}")
        val flow = MutableStateFlow<Map<ZonedDateTime, Double>>(emptyMap())
        val outputMap = mutableMapOf<ZonedDateTime, Double>()
        downloadedModels.first().firstOrNull { it.name == modelName }?.let { model ->
            val modelFile = model.file
            if (modelFile != null) {
                sensorEventInteractor.getSyncedEvents(sessionUUID).first { sensorEvents ->
                    sensorEvents?.let {
                        val advancedImuSensorData = SensorFusion.fuseSensors(
//                            interval = 80.milliseconds + Random.nextLong(-10, 10).milliseconds,
                            accRaw = sensorEvents.filter {
                                it.type.toInt() == if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    Sensor.TYPE_LINEAR_ACCELERATION else Sensor.TYPE_ACCELEROMETER
                            },
                            accSmooth = emptyList(),
                            dirX = emptyList(), // FIXME: replace with real values
                            dirY = emptyList(), // FIXME: replace with real values
                            dirZ = emptyList(), //sensorEvents.filter { it.type.toInt() == Sensor.TYPE_ROTATION_VECTOR },
                            angVel = emptyList(), // FIXME: replace with real values
                            angAccel = emptyList(), // FIXME: replace with real values
                        )
                        val interpreter = Interpreter(modelFile)
                        advancedImuSensorData.chunked(400) { chunk ->
                            interpreter.allocateTensors()
                            Timber.v(
                                "Running model with input shape ${
                                    interpreter.getInputTensor(0).shape().joinToString()
                                } and output shape ${
                                    interpreter.getOutputTensor(0).shape().joinToString()
                                } on chunk of size ${chunk.size}"
                            )
                            if (chunk.size < 400) {
                                Timber.v("Chunk size is less than 400, skipping...")
                                return@chunked chunk
                            }
                            val startMilli = chunk.first().timestamp
                            chunk.map {
                                listOf((it.timestamp - startMilli) / 1000.0).toTypedArray() + // Added a bit of noise
                                        it.accRaw.toList().toTypedArray() +
                                        it.accSmooth.toList().toTypedArray() +
                                        it.dirX.toList().toTypedArray() +
                                        it.dirY.toList().toTypedArray() +
                                        it.dirZ.toList().toTypedArray()
//                            it.angVel.toList().toTypedArray() +
//                            it.angAccel.toList().toTypedArray()
                            }.map { array -> array.map { it.toFloat() } }.toTypedArray()
                                .let { events ->
                                    val input = ByteBuffer.allocate(8070 * 400 * 16 * java.lang.Float.SIZE / 8).order(ByteOrder.nativeOrder())
                                    for (i in 0 until 8070) {
                                        events.forEach { sensorValues ->
                                            sensorValues.forEach { input.putFloat(it) }
                                        }
                                    }
                                    val outputSize = 8070 * java.lang.Float.SIZE / 8
                                    val output = ByteBuffer.allocate(outputSize).order(ByteOrder.nativeOrder())
                                    interpreter.run(input, output)
                                    output.rewind()
                                    val outputs = mutableListOf<Float>()
                                    for (i in 0 until output.asFloatBuffer().capacity()) {
                                        outputs.add(output.asFloatBuffer()[i])
                                    }
                                    Timber.v("Model outputs: ${outputs.distinct().joinToString()}")
                                    chunk.forEach { advancedImuSensorData ->
                                        outputMap[Instant.ofEpochMilli(advancedImuSensorData.timestamp).toZonedDateTime()] = outputs[0].toDouble()
                                    }
                                    flow.update { outputMap }
                                }
                            interpreter.resetVariableTensors()
                        }
                    }
                    sensorEvents != null
                }
            }
            modelFile != null // The model is downloaded
        }
        return flow.asStateFlow()
    }
}