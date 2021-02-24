package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.definition.*
import org.ozinger.ika.serialization.ModeDefs
import org.ozinger.ika.serialization.ModeStringDescriptor
import kotlin.reflect.KProperty1

sealed class ModeModificationSerializer(private val modeProperty: KProperty1<ModeDefs, ModeDefinition>) :
    KSerializer<ModeModification>, KoinComponent {
    override val descriptor = ModeStringDescriptor("ModeModification")

    private val modeDefs: ModeDefs by inject()
    private val modeDefinition by lazy { modeProperty.get(modeDefs) }

    override fun serialize(encoder: Encoder, value: ModeModification) = encoder.encodeStructure(descriptor) {
        val values = mutableListOf<String>()

        val sb = StringBuilder()

        val f = { op: Char, modes: Modes ->
            if (modes.isNotEmpty()) {
                sb.append(op)
                modes.forEach { mode ->
                    when (mode) {
                        is Mode -> {
                            sb.append(mode.mode)
                            mode.param?.let { values.add(it) }
                        }
                        is MemberMode -> {
                            sb.append(mode.mode)
                            values.add(mode.target.value)
                        }
                    }
                }
            }
        }

        f('+', value.adding)
        f('-', value.removing)

        if (sb.isEmpty()) {
            sb.append('+')
        }

        values.add(0, sb.toString())

        encodeStringElement(descriptor, 0, values.joinToString(" "))
    }

    override fun deserialize(decoder: Decoder): ModeModification {
        val adding: MutableModes = mutableSetOf()
        val removing: MutableModes = mutableSetOf()
        var current = adding

        val modeString = decoder.decodeString()
        for (c in modeString) {
            when (c) {
                '+' -> current = adding
                '-' -> current = removing
                else -> {
                    if (c in modeDefinition.stackable ||
                        c in modeDefinition.parameterized ||
                        (modeDefinition == modeDefs.channel && c in modeDefs.member.parameterized) ||
                        (current == adding && c in modeDefinition.parameterizedAdd)
                    ) {
                        val param = decoder.decodeString()
                        try {
                            current.add(MemberMode(param, c))
                        } catch (e: IllegalIdentifierException) {
                            current.add(Mode(c, param))
                        }
                    } else {
                        current.add(Mode(c))
                    }
                }
            }
        }

        return ModeModification(adding, removing)
    }
}

object ChannelModeModificationSerializer : ModeModificationSerializer(ModeDefs::channel)
object UserModeModificationSerializer : ModeModificationSerializer(ModeDefs::user)
