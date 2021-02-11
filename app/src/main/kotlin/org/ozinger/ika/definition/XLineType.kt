package org.ozinger.ika.definition

import kotlinx.serialization.Serializable
import org.ozinger.ika.serialization.serializer.EnumValueSerializer

object XLineTypeSerializer : EnumValueSerializer<XLineType>(XLineType::class, XLineType.UNKNOWN)

@Serializable(with = XLineTypeSerializer::class)
enum class XLineType(override val value: String) : ValuedEnum {
    GLOBAL_HOST_BAN("G"),
    GLOBAL_NICK_BAN("Q"),
    LOCAL_HOST_BAN("K"),
    LOCAL_IP_BAN("Z"),
    LOCAL_HOST_BAN_EXCEPTION("E"),
    UNKNOWN(""),
}