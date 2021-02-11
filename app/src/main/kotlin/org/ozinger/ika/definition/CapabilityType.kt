package org.ozinger.ika.definition

import kotlinx.serialization.Serializable
import org.ozinger.ika.serialization.serializer.EnumValueSerializer

object CapabilityTypeSerializer : EnumValueSerializer<CapabilityType>(CapabilityType::class, CapabilityType.UNKNOWN)

@Serializable(with = CapabilityTypeSerializer::class)
enum class CapabilityType(override val value: String) : ValuedEnum {
    START("START"),
    MODULES("MODULES"),
    MODULES_SUPPORT("MODSUPPORT"),
    CHANNEL_MODES("CHANMODES"),
    USER_MODES("USERMODES"),
    CAPABILITIES("CAPABILITIES"),
    END("END"),
    UNKNOWN(""),
}
