package org.ozinger.ika.definition

import kotlinx.serialization.Serializable
import org.ozinger.ika.serialization.serializer.EnumValueSerializer

object XLineTypeSerializer : EnumValueSerializer<XLineType>(XLineType::class, XLineType.Unknown)

@Serializable(with = XLineTypeSerializer::class)
enum class XLineType(override val value: String) : ValuedEnum {
    GlobalHostBan("G"),
    GlobalNickBan("Q"),
    LocalHostBan("K"),
    LocalIpBan("Z"),
    LocalHostBanException("E"),
    Unknown(""),
}