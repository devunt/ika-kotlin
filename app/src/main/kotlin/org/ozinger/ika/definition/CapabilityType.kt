package org.ozinger.ika.definition

import kotlinx.serialization.Serializable
import org.ozinger.ika.serialization.serializer.EnumValueSerializer

object CapabilityTypeSerializer : EnumValueSerializer<CapabilityType>(CapabilityType::class, CapabilityType.Unknown)

@Serializable(with = CapabilityTypeSerializer::class)
enum class CapabilityType(override val value: String) : ValuedEnum {
    Start("START"),
    Modules("MODULES"),
    ModuleSupport("MODSUPPORT"),
    ChannelModes("CHANMODES"),
    UserModes("USERMODES"),
    Capabilities("CAPABILITIES"),
    End("END"),
    Unknown(""),
}
