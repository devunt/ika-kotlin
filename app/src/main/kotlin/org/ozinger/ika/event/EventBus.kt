package org.ozinger.ika.event

import kotlinx.coroutines.channels.Channel
import org.ozinger.ika.networking.Packet

abstract class EventBus {
    protected abstract val channel: Channel<Packet>

    suspend fun get() = channel.receive()

    suspend fun put(packet: Packet) {
        channel.send(packet)
    }
}