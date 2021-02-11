package org.ozinger.ika.serialization.encoding

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import org.ozinger.ika.serialization.context

class PacketDecoder(value: String) : AbstractDecoder() {
    override val serializersModule
        get() = context

    private val hasSender = value.startsWith(":")
    private var elementIndex = if (hasSender) 0 else 1

    private val values = if (hasSender) {
        value.substring(1).split(" ", limit = 2)
    } else {
        listOf("", value)
    }

    override fun decodeValue(): Any {
        return values[elementIndex - 1]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex >= descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }
}