package illyan.jay.data.network.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import illyan.jay.data.network.serializers.TimestampSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class FirestoreLocation(
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Timestamp,
    val latitude: Float,
    val longitude: Float,
    var speed: Float = Float.MIN_VALUE,
    var accuracy: Int = Int.MIN_VALUE,
    var bearing: Int = Int.MIN_VALUE,
    var bearingAccuracy: Int = Int.MIN_VALUE, // in degrees
    var altitude: Int = Int.MIN_VALUE,
    var speedAccuracy: Float = Float.MIN_VALUE, // in meters per second
    var verticalAccuracy: Int = Int.MIN_VALUE, // in meters
) : Parcelable