package org.ozinger.ika.event

import kotlinx.coroutines.channels.Channel
import org.ozinger.ika.networking.Packet

class PacketReceiver : EventBus() {
    override val channel: Channel<Packet> = Channel(Channel.RENDEZVOUS)
}