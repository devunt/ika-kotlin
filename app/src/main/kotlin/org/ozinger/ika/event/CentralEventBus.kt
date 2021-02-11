package org.ozinger.ika.event

import kotlinx.coroutines.channels.Channel
import org.ozinger.ika.command.Command
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.networking.Origin
import org.ozinger.ika.networking.Packet

object CentralEventBus {
    object Incoming {
        private val channel = Channel<Packet>(Channel.RENDEZVOUS)

        suspend fun get() = channel.receive()

        suspend fun received(packet: Packet) {
            channel.send(packet)
        }
    }

    object Outgoing {
        private val channel = Channel<Packet>(Channel.RENDEZVOUS)

        suspend fun get() = channel.receive()

        suspend fun send(packet: Packet) {
            channel.send(packet)
        }

        suspend fun send(block: suspend SenderDSL.() -> Unit) {
            SenderDSL().block()
        }

        class SenderDSL {
            suspend fun sendDirect(command: Command) {
                send(Packet(Origin.Direct, command))
            }

            suspend fun sendAsServer(command: Command) {
                send(Packet(Origin.Server.MYSELF, command))
            }

            suspend fun sendAsUser(userId: UniversalUserId, command: Command) {
                send(Packet(Origin.User(userId), command))
            }
        }
    }
}