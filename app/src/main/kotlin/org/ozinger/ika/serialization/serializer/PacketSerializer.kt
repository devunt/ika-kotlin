package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer
import org.ozinger.ika.command.Command
import org.ozinger.ika.definition.*
import org.ozinger.ika.networking.Packet
import org.ozinger.ika.networking.Serializers

class PacketSerializer : KSerializer<Packet> {
    override val descriptor = buildClassSerialDescriptor("Packet") {
        element<Identifier?>("sender")
        element<Command>("command")
    }

    override fun serialize(encoder: Encoder, value: Packet) = encoder.encodeStructure(descriptor) {
        when (value.sender) {
            is ServerId, is UniversalUserId -> encodeStringElement(descriptor, 0, ":${value.sender.value}")
            null -> {
            }
            else -> throw SerializationException("Only ServerId and UniversalUserId or null can be Packet.sender")
        }
        encodeSerializableElement(descriptor, 1, serializer(), value.command)
    }

    override fun deserialize(decoder: Decoder): Packet = decoder.decodeStructure(descriptor) {
        var sender: Identifier? = null
        lateinit var command: Command
        loop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> {
                    sender = decodeSerializableElement(descriptor, 0, serializer<Identifier>())
                    if (!(sender is ServerId || sender is UniversalUserId)) {
                        throw SerializationException("Only ServerId and UniversalUserId or null can be Packet.sender")
                    }
                }
                1 -> {
                    val commandString = decodeStringElement(descriptor, 1)
                    command = Serializers.decodeCommandFromString(commandString)
                }
                else -> throw SerializationException("Unexpected index $index")
            }
        }
        return Packet(sender, command)
    }
}