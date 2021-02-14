package org.ozinger.ika.serialization

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind

class ModeStringDescriptor(override val serialName: String, trailing: Boolean = false) : SerialDescriptor {
    override val elementsCount = 1

    override val kind = SerialKind.CONTEXTUAL

    override fun getElementAnnotations(index: Int) = emptyList<Annotation>()

    override fun getElementDescriptor(index: Int) = String.serializer().descriptor

    override fun getElementIndex(name: String) = 0

    override fun getElementName(index: Int) = "modestring"

    override fun isElementOptional(index: Int) = false

    override val annotations = if (trailing) emptyList() else listOf(annotation)

    companion object {
        val annotation = object : Annotation {}
    }
}
