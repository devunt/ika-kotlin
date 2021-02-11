package org.ozinger.ika.serialization.encoding

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.AbstractEncoder
import org.ozinger.ika.serialization.context

class SpacedEncoder : AbstractEncoder() {
    override val serializersModule
        get() = context

    private val list = mutableListOf<String>()
    private var trailing = false

    val encodedValue
        get() = list.joinToString(" ")

    override fun encodeValue(value: Any) {
        val v = value.toString()
        list.add(
            when {
                v.contains(' ') -> {
                    trailing = true; ":$v"
                }
                trailing -> throw SerializationException("Parameter contains the whitespace character should be used once at the last position")
                else -> v
            }
        )
    }

    override fun encodeNull() {}
}