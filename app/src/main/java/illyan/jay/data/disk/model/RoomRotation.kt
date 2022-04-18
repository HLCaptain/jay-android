package illyan.jay.data.disk.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import illyan.jay.data.disk.model.RoomSession

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
    val id: Int = 0,
    val sessionId: Int,
    val time: Long, // in millis
    val accuracy: Int, // enum
    val x: Float,
    val y: Float,
    val z: Float
)