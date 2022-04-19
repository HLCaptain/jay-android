package illyan.jay.data.disk.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rotation",
    foreignKeys = [ForeignKey(
        entity = RoomSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"]
    )],
    indices = [Index(value = ["sessionId"])]
)
data class RoomRotation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val time: Long, // in millis
    val accuracy: Int, // enum
    val x: Float,
    val y: Float,
    val z: Float
)