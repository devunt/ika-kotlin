package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer
import org.ozinger.ika.command.Command
import org.ozinger.ika.definition.Identifier
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.networking.Origin
import org.ozinger.ika.networking.Packet
import org.ozinger.ika.networking.Serializers

class PacketSerializer : KSerializer<Packet> {
    override val descriptor = buildClassSerialDescriptor("Packet") {
        element<Origin>("origin")
        element<Command>("command")
    }

    override fun serialize(encoder: Encoder, value: Packet) = encoder.encodeStructure(descriptor) {
        when (value.origin) {
            is Origin.Server -> encodeStringElement(descriptor, 0, ":${value.origin.serverId.value}")
            is Origin.User -> encodeStringElement(descriptor, 0, ":${value.origin.userId.value}")
            Origin.Direct -> {
            }
        }
        encodeSerializableElement(descriptor, 1, serializer(), value.command)
    }

    override fun deserialize(decoder: Decoder): Packet = decoder.decodeStructure(descriptor) {
        var origin: Origin = Origin.Direct
        var command: Command? = null
        loop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> {
                    val senderId = decodeSerializableElement(descriptor, 0, serializer<Identifier>())
                    origin = when (senderId) {
                        is ServerId -> Origin.Server(senderId)
                        is UniversalUserId -> Origin.User(senderId)
                        else -> throw SerializationException("Sender id must be 3 or 9 characters")
                    }
                }
                1 -> {
                    val commandString = decodeStringElement(descriptor, 1)
                    command = Serializers.decodeCommandFromString(commandString)
                }
                else -> throw SerializationException("Unexpected index $index")
            }
        }
        return Packet(origin, command!!)
    }
}