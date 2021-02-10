package org.ozinger.ika.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ozinger.ika.definition.ValuedEnum
import kotlin.reflect.KClass

open class EnumValueSerializer<E>(
    private val kClass: KClass<E>,
    private val defaultValue: E,
) : KSerializer<E> where E : Enum<E>, E : ValuedEnum {
    override val descriptor = PrimitiveSerialDescriptor(kClass.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: E) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): E {
        val value = decoder.decodeString()
        return kClass.java.enumConstants.find { it.value == value } ?: defaultValue
    }
}