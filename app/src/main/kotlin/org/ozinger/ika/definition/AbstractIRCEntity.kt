package org.ozinger.ika.definition

abstract class AbstractIRCEntity {
    val metadata = mutableMapOf<String, String>()
    val modes: MutableModes = mutableSetOf()

    abstract fun applyModeModification(modeModification: ModeModification)
}