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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
        val flow = MutableSharedFlow<Map<ZonedDateTime, Double>>(extraBufferCapacity = 1)
        val outputMap = mutableMapOf<ZonedDateTime, Double>()
        downloadedModels.first().firstOrNull { it.name == modelName }?.let { model ->
            val modelFile = model.file
            if (modelFile != null) {
                val sensorEvents = sensorEventInteractor.getSensorEvents(sessionUUID).first()
                val advancedImuSensorData = SensorFusion.fuseSensors(
                    accRaw = sensorEvents.filter {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            it.type.toInt() == Sensor.TYPE_LINEAR_ACCELERATION
                        else
                            false
                    },
                    accSmooth = sensorEvents.filter { it.type.toInt() == Sensor.TYPE_ACCELEROMETER },
                    dirX = emptyList(),
                    dirY = emptyList(),
                    dirZ = emptyList(),
                    angVel = emptyList(),
                    angAccel = emptyList(),
                )
                val interpreter = Interpreter(modelFile)
                interpreter.allocateTensors()
                advancedImuSensorData.chunked(400) { chunk ->
                    // FIXME: transform input to model shape
                    Timber.v("Running model with input shape ${interpreter.getInputTensor(0).shape().joinToString()} on chunk of size ${chunk.size}")
                    Timber.v("Running model with output shape ${interpreter.getOutputTensor(0).shape().joinToString()} on chunk of size ${chunk.size}")
                    if (chunk.size < 400) {
                        Timber.d("Chunk size is less than 400, skipping...")
                        return@chunked chunk
                    }
                    val startTimestamp = chunk.first().timestamp
                    chunk.map {
                        listOf((it.timestamp - startTimestamp) / 1000.0).toTypedArray() +
                            it.accRaw.toList().toTypedArray() +
                            it.accSmooth.toList().toTypedArray() +
                            it.dirX.toList().toTypedArray() +
                            it.dirY.toList().toTypedArray() +
                            it.dirZ.toList().toTypedArray()
//                            it.angVel.toList().toTypedArray() +
//                            it.angAccel.toList().toTypedArray()
                    }.map { array -> array.map { it.toFloat() } }.toTypedArray().let { events ->
                        val input = ByteBuffer.allocateDirect(8070 * 400 * 16 * java.lang.Float.SIZE / 8).order(ByteOrder.nativeOrder())
                        for (i in 0 until 8070) {
                            events.forEach { sensorValues -> sensorValues.forEach { input.putFloat(it) }}
                        }
                        val outputSize = 8070 * java.lang.Float.SIZE / 8
                        val output = ByteBuffer.allocateDirect(outputSize).order(ByteOrder.nativeOrder())
                        interpreter.run(input, output)
                        output.rewind()
                        val outputs = mutableListOf<Float>()
                        for (i in 0 until output.asFloatBuffer().capacity()) {
                            outputs.add(output.asFloatBuffer()[i])
                        }
                        Timber.v("Model output: ${outputs[0]}")
                        chunk.forEach { advancedImuSensorData ->
                            outputMap[Instant.ofEpochMilli(advancedImuSensorData.timestamp).toZonedDateTime()] = outputs[0].toDouble()
                        }
                        coroutineScopeIO.launch {
                            flow.emit(outputMap)
                        }
                    }
                }
                // TODO: Use advancedImuSensorData and get filtered driver aggression

                // TODO: emit model outputs
                //  val aggressions = mutableMapOf<ZonedDateTime, Double>()
                //  // ...Run Model on Chunk
                //  // ...Add Model output to aggressions (zonedDateTime to modelOutput)
                //  // ...emit(aggressions)
            }
            modelFile != null // The model is downloaded
        }
        return flow
    }
}