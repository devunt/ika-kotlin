package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ozinger.ika.definition.Mode
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.definition.ModeModification
import org.ozinger.ika.definition.Modes
import kotlin.reflect.KProperty0

open class ModeModificationSerializer(private val modeProperty: KProperty0<ModeDefinition>) :
    KSerializer<ModeModification> {
    override val descriptor = buildClassSerialDescriptor("ModeModification") {
        element<String>("modes")
    }

    protected val modeDefinition by lazy { modeProperty.get() }

    override fun serialize(encoder: Encoder, value: ModeModification) {
        throw NotImplementedError()
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
