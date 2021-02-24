package org.ozinger.ika.store

import org.ozinger.ika.definition.Channel
import org.ozinger.ika.definition.ChannelName

class ChannelStore {
    private val channels = mutableMapOf<ChannelName, Channel>()

    fun add(channel: Channel) {
        require(!exists(channel.name))
        channels[channel.name] = channel
    }

    fun get(channelName: ChannelName) = channels.getValue(channelName)

    fun exists(channelName: ChannelName) = channels.containsKey(channelName)

    fun del(channelName: ChannelName) {
        check(get(channelName).isEmpty)
        channels.remove(channelName)
    }
}