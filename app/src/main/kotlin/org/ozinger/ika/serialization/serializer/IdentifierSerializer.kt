package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ozinger.ika.definition.*

class IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Identifier) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): Identifier {
        val value = decoder.decodeString()

        return when {
            value == "*" -> Wildcard()
            value.startsWith('#') -> ChannelName(value)
            value.length == 3 -> ServerId(value)
            value.length == 6 -> ServerUserId(value)
            value.length == 9 -> UniversalUserId(value)
            else -> throw SerializationException("Invalid identifier")
        }
    }
}