package org.ozinger.ika.serialization.encoding

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import org.ozinger.ika.serialization.context

class SpacedDecoder(val value: String) : AbstractDecoder() {
    override val serializersModule = context

    private val values = value.split(" ").toMutableList()
    private var elementIndex = 0

    override fun decodeValue(): Any {
        if (values.isEmpty()) {
            return ""
        }

        val value = values[0]

        if (value.startsWith(":")) {
            val r = values.joinToString(" ").substring(1)
            values.clear()
            return r
        }

        values.removeFirst()
        return value
    }

    override fun decodeNotNullMark() = values.size > 0

    override fun decodeInt(): Int = decodeString().toInt()
    override fun decodeLong(): Long = decodeString().toLong()
    override fun decodeString(): String = decodeValue() as String
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeInt()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun decodeSequentially() = true
}