package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeLong(value.toEpochSecond(ZoneOffset.UTC))

    override fun deserialize(decoder: Decoder) = LocalDateTime.ofEpochSecond(decoder.decodeLong(), 0, ZoneOffset.UTC)
}