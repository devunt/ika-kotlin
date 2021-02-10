package org.ozinger.ika.networking

import kotlinx.serialization.Serializable
import org.ozinger.ika.command.Command
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.serialization.serializer.PacketSerializer

@Serializable(with = PacketSerializer::class)
data class Packet(
    val origin: Origin,
    val command: Command,
) {
    companion object {
        fun build(command: Command) = Packet(Origin.Direct, command)
        fun build(serverId: ServerId, command: Command) = Packet(Origin.Server(serverId), command)
        fun build(userId: UniversalUserId, command: Command) = Packet(Origin.User(userId), command)
    }
}
