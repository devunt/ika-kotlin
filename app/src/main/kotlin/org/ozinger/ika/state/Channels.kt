package org.ozinger.ika.state

import org.ozinger.ika.definition.Channel
import org.ozinger.ika.definition.ChannelName

object Channels {
    private val channels = mutableMapOf<ChannelName, Channel>()

    fun add(channel: Channel) {
        require(!exists(channel.name))
        channels[channel.name] = channel
    }

    fun get(channelName: ChannelName) = channels.getValue(channelName)

    fun exists(channelName: ChannelName) = channels.containsKey(channelName)

    fun iterate(block: (Channel) -> Unit) {
        channels.forEach { block(it.value) }
    }

    fun del(channelName: ChannelName) {
        channels.remove(channelName)
    }
}