package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.definition.MemberMode
import org.ozinger.ika.definition.ModeModification
import org.ozinger.ika.definition.MutableModes
import org.ozinger.ika.serialization.ModeDefs
import org.ozinger.ika.serialization.ModeStringDescriptor

class ChannelMemberDeclarationSerializer : KSerializer<ModeModification>, KoinComponent {
    override val descriptor = ModeStringDescriptor("ChannelMemberDeclaration", trailing = true)

    private val modeDefs: ModeDefs by inject()

    override fun serialize(encoder: Encoder, value: ModeModification) = encoder.encodeStructure(descriptor) {
        val memberModes = mutableMapOf<String, MutableSet<Char>>()

        value.adding.filterIsInstance<MemberMode>().forEach { mode ->
            val set = memberModes.getOrPut(mode.target.value, ::mutableSetOf)
            mode.mode?.let { set.add(it) }
        }

        val memberString = memberModes.map { (uuid, modes) ->
            StringBuilder().apply {
                modes.map(::append)
                append(",")
                append(uuid)
            }.toString()
        }.joinToString(" ")

        encodeStringElement(descriptor, 0, memberString)
    }

    override fun deserialize(decoder: Decoder): ModeModification {
        val adding: MutableModes = mutableSetOf()

        val members = decoder.decodeString().split(" ")
        for (member in members) {
            val (modes, uuid) = member.split(",")
            for (mode in modes) {
                if (mode !in modeDefs.member.parameterized) {
                    throw SerializationException("Invalid member mode: $mode")
                }
                adding.add(MemberMode(uuid, mode))
            }
            if (modes.isEmpty()) {
                adding.add(MemberMode(uuid))
            }
        }

        return ModeModification(adding = adding)
    }
}
