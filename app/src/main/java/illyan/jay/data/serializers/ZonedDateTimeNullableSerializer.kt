package illyan.jay.data.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime

object ZonedDateTimeNullableSerializer : KSerializer<ZonedDateTime?> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("java.time.ZonedDateTime?", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime? {
        val decoded = decoder.decodeString()
        if (decoded == "null") return null
        return ZonedDateTime.parse(decoded)
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime?) {
        encoder.encodeString(value.toString())
    }
}
