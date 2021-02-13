package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import org.ozinger.ika.definition.Mode
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.definition.ModeModification
import org.ozinger.ika.definition.Modes
import org.ozinger.ika.serialization.ModeStringDescriptor
import kotlin.reflect.KProperty0

open class ModeModificationSerializer(private val modeProperty: KProperty0<ModeDefinition>) :
    KSerializer<ModeModification> {
    override val descriptor = ModeStringDescriptor("ModeModification")

    protected val modeDefinition by lazy { modeProperty.get() }

    override fun serialize(encoder: Encoder, value: ModeModification) = encoder.encodeStructure(descriptor) {
        val values = mutableListOf<String>()

        val sb = StringBuilder()

        val f = { op: Char, modes: Set<Mode>? ->
            if (!modes.isNullOrEmpty()) {
                sb.append(op)
                modes.forEach { mode ->
                    sb.append(mode.mode)
                    mode.param?.let { values.add(it) }
                }
            }
        }

        f('+', value.adding)
        f('-', value.removing)

        values.add(0, sb.toString())

        encodeStringElement(descriptor, 0, values.joinToString(" "))
    }

    override fun deserialize(decoder: Decoder): ModeModification {
        val adding: Modes = mutableSetOf()
        val removing: Modes = mutableSetOf()
        var current = adding

        val modeString = decoder.decodeString()
        for (c in modeString) {
            when (c) {
                '+' -> current = adding
                '-' -> current = removing
                else -> {
                    if (c in modeDefinition.stackable || c in modeDefinition.parameterized || (current == adding && c in modeDefinition.parameterizedAdd)) {
                        current.add(Mode(c, decoder.decodeString()))
                    } else {
                        current.add(Mode(c))
                    }
                }
            }
        }

        return ModeModification(adding, removing)
    }
}
