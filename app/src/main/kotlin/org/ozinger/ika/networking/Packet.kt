package org.ozinger.ika.networking

import kotlinx.serialization.Serializable
import org.ozinger.ika.command.Command
import org.ozinger.ika.definition.Identifier
import org.ozinger.ika.serialization.serializer.PacketSerializer

@Serializable(with = PacketSerializer::class)
data class Packet(
    val sender: Identifier?,
    val command: Command,
)
