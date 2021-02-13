package org.ozinger.ika.serialization.encoding

import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import org.ozinger.ika.serialization.ModeStringDescriptor
import org.ozinger.ika.serialization.context

open class SpacedEncoder : AbstractEncoder() {
    override val serializersModule
        get() = context

    protected val list = mutableListOf<String>()
    private var trailing = false

    val encodedValue
        get() = list.joinToString(" ")

    override fun encodeValue(value: Any) {
        val v = value.toString()
        list.add(
            when {
                trailing -> throw SerializationException("Parameter contains the whitespace character should be used once at the last position")
                v.contains(' ') -> {
                    trailing = true; ":$v"
                }
                else -> v
            }
        )
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor.annotations.contains(ModeStringDescriptor.annotation)) {
            return WhitespaceEncoder()
        }
        return this
    }

    override fun encodeNull() {}

    inner class WhitespaceEncoder : SpacedEncoder() {
        override fun encodeValue(value: Any) {
            list.add(value.toString())
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            this@SpacedEncoder.list.add(this.encodedValue)
        }
    }
}

