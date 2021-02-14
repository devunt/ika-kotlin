package org.ozinger.ika.event

import kotlinx.coroutines.channels.Channel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.command.Command
import org.ozinger.ika.configuration.Configuration
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.networking.Packet

class PacketSender : EventBus(), KoinComponent {
    override val channel: Channel<Packet> = Channel(Channel.RENDEZVOUS)
    private val configuration: Configuration by inject()

    suspend fun sendDirect(command: Command) {
        put(Packet(null, command))
    }

    suspend fun sendAsServer(command: Command) {
        put(Packet(configuration.server.id, command))
    }

    suspend fun sendAsUser(userId: UniversalUserId, command: Command) {
        put(Packet(userId, command))
    }

//    suspend inline fun send(block: PacketSender.() -> Unit) {
//        block()
//    }
}