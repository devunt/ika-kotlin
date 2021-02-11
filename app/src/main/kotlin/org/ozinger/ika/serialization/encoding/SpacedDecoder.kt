package org.ozinger.ika.serialization.encoding

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import org.ozinger.ika.serialization.context

class SpacedDecoder(val value: String) : AbstractDecoder() {
    override val serializersModule
        get() = context

    private val values = value.split(" ").toMutableList()

    override fun decodeValue(): Any {
        val value = values.first()

        if (value.startsWith(":")) {
            return values.joinToString(" ").substring(1).also { values.clear() }
        }

        values.removeFirst()
        return value
    }

    override fun decodeInt(): Int = decodeString().toInt()
    override fun decodeLong(): Long = decodeString().toLong()
    override fun decodeString(): String = decodeValue().toString()

    override fun decodeNotNullMark() = values.isNotEmpty()

    override fun decodeElementIndex(descriptor: SerialDescriptor) = 0
    override fun decodeSequentially() = true
}