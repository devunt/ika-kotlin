package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ozinger.ika.definition.Mode
import org.ozinger.ika.definition.ModeModification
import org.ozinger.ika.definition.Modes
import org.ozinger.ika.state.ModeDefinitions

open class MemberModeModificationSerializer : KSerializer<ModeModification> {
    override val descriptor = buildClassSerialDescriptor("MemberModeModification") {
        element<String>("modes")
    }

    private val modeDefinition by lazy { ModeDefinitions::member.get() }

    override fun serialize(encoder: Encoder, value: ModeModification) {
        throw NotImplementedError()
    }

    override fun deserialize(decoder: Decoder): ModeModification {
        val adding: Modes = mutableSetOf()

        val members = decoder.decodeString().split(" ")
        for (member in members) {
            val (modes, uuid) = member.split(",")
            for (mode in modes) {
                if (mode !in modeDefinition.parameterized) {
                    throw SerializationException("Invalid member mode: $mode")
                }
                adding.add(Mode(mode, uuid).apply { isMemberMode = true })
            }
            if (modes.isEmpty()) {
                adding.add(Mode(' ', uuid).apply { isMemberMode = true })
            }
        }

        return ModeModification(adding = adding)
    }
}
