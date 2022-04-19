package illyan.jay.data.disk.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location",
    foreignKeys = [ForeignKey(
        entity = RoomSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"]
    )],
    indices = [Index(value = ["sessionId"])]
)
data class RoomLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val time: Long, // in millis
    val speed: Float,
    val speedAccuracy: Float, // in meters per second
    val bearing: Float,
    val bearingAccuracy: Float, // in degrees
    val altitude: Double,
    val verticalAccuracy: Float // in meters
)