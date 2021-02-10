package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Duration

class DurationSerializer : KSerializer<Duration> {
    override val descriptor = PrimitiveSerialDescriptor(Duration::class.qualifiedName!!, PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Duration) = encoder.encodeLong(value.seconds)
    override fun deserialize(decoder: Decoder) = Duration.ofSeconds(decoder.decodeLong())
}