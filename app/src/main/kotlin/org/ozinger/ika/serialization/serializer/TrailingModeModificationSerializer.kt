package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.encoding.Decoder
import org.ozinger.ika.definition.*
import org.ozinger.ika.state.ModeDefinitions
import kotlin.reflect.KProperty0

open class TrailingModeModificationSerializer(modeProperty: KProperty0<ModeDefinition>) :
    ModeModificationSerializer(modeProperty) {
    override fun deserialize(decoder: Decoder): ModeModification {
        val adding: Modes = mutableSetOf()
        val removing: Modes = mutableSetOf()
        var current = adding

        val tokens = mutableListOf<String>()
        while (decoder.decodeNotNullMark()) {
            tokens.add(decoder.decodeString())
        }

        val modeString = tokens.removeFirst()
        for (c in modeString) {
            when (c) {
                '+' -> current = adding
                '-' -> current = removing
                else -> {
                    if (c in modeDefinition.stackable || c in modeDefinition.parameterized || (current == adding && c in modeDefinition.parameterizedAdd)) {
                        current.add(Mode(c, tokens.removeFirst()))
                    } else if (c in ModeDefinitions.member.parameterized && try {
                            UniversalUserId(tokens.first()); true
                        } catch (e: IllegalArgumentException) {
                            false
                        }
                    ) {
                        current.add(Mode(c, tokens.removeFirst()).apply { isMemberMode = true })
                    } else {
                        current.add(Mode(c))
                    }
                }
            }
        }

        return ModeModification(adding, removing)
    }
}
