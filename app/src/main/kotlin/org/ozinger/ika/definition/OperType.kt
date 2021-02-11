package org.ozinger.ika.definition

import kotlinx.serialization.Serializable
import org.ozinger.ika.serialization.serializer.EnumValueSerializer

object OperTypeSerializer : EnumValueSerializer<OperType>(OperType::class, OperType.Unknown)

@Serializable(with = OperTypeSerializer::class)
enum class OperType(override val value: String) : ValuedEnum {
    NETADMIN("NetAdmin"),
    SERVICE("Services"),
    BOT("Bot"),
    Unknown(""),
}