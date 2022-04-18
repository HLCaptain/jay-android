package illyan.jay.data.disk

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import illyan.jay.data.disk.dao.AccelerationDao
import illyan.jay.data.disk.dao.LocationDao
import illyan.jay.data.disk.dao.RotationDao
import illyan.jay.data.disk.dao.SessionDao
import illyan.jay.data.disk.model.RoomAcceleration
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.data.disk.model.RoomRotation
import illyan.jay.data.disk.model.RoomSession

@Database(
    entities = [
        RoomSession::class,
        RoomLocation::class,
        RoomAcceleration::class,
        RoomRotation::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(
    RoomConverter::class
)
abstract class JayDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun locationDao(): LocationDao
    abstract fun accelerationDao(): AccelerationDao
    abstract fun rotationDao(): RotationDao

    companion object {
        const val DB_NAME = "jay.db"
    }
}