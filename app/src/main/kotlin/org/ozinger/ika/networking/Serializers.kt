package org.ozinger.ika.networking

import kotlinx.serialization.serializer
import org.ozinger.ika.command.Command
import org.ozinger.ika.serialization.encoding.PacketDecoder
import org.ozinger.ika.serialization.encoding.SpacedDecoder
import org.ozinger.ika.serialization.encoding.SpacedEncoder

class Serializers {
    companion object {
        fun encodeCommandToString(value: Command): String {
            val encoder = SpacedEncoder()
            encoder.encodeSerializableValue(serializer(), value)
            return encoder.encodedValue
        }

        fun decodeCommandFromString(value: String): Command {
            val decoder = SpacedDecoder(value)
            return decoder.decodeSerializableValue(serializer())
        }

        fun encodePacketToString(value: Packet): String {
            val encoder = SpacedEncoder()
            encoder.encodeSerializableValue(serializer(), value)
            return encoder.encodedValue
        }

        fun decodePacketFromString(value: String): Packet {
            val decoder = PacketDecoder(value)
            return decoder.decodeSerializableValue(serializer())
        }
    }
}