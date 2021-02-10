package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ozinger.ika.command.UnknownCommand
import org.ozinger.ika.serialization.encoding.SpacedDecoder

object UnknownCommandSerializer : KSerializer<UnknownCommand> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(UnknownCommand::class.simpleName!!) {
            element<String>("command")
            element<String>("param")
        }

    override fun serialize(encoder: Encoder, value: UnknownCommand) {
        throw NotImplementedError()
    }

    override fun deserialize(decoder: Decoder): UnknownCommand {
        if (decoder is SpacedDecoder) {
            val splitted = decoder.value.split(" ", limit = 2)
            return UnknownCommand(splitted[0], splitted.getOrNull(1) ?: "")
        }
        throw SerializationException()
    }
}