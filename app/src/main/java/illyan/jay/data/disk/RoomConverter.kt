package illyan.jay.data.disk

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class RoomConverter {
    @TypeConverter
    fun toDate(value: String?): Date? {
        return value?.let {
            SimpleDateFormat("HH:mm:ss.SSS zzz MMM dd, yyyy", Locale.US).parse(it)
        }
    }

    @TypeConverter
    fun toString(value: Date?): String? {
        return value?.let { SimpleDateFormat("HH:mm:ss.SSS zzz MMM dd, yyyy", Locale.US).format(it) }
    }
}