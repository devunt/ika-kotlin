package org.ozinger.ika.handler.state

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.*
import org.ozinger.ika.handler.AbstractHandler
import org.ozinger.ika.state.Channels
import org.ozinger.ika.state.Users
import java.time.LocalDateTime
import java.time.ZoneOffset

@Handler
class ChannelStateHandler : AbstractHandler() {
    @Handler
    fun userJoinedChannel(sender: ServerId, command: FJOIN) {
        if (Channels.exists(command.channelName)) {
            val channel = Channels.get(command.channelName)
            if (channel.timestamp > command.timestamp) {
                channel.modes.clear()
                channel.timestamp = command.timestamp
            }
            channel.applyModeModification(command.channelModeModification)
            channel.applyModeModification(command.memberModeModification)
        } else {
            Channels.add(
                Channel(
                    command.channelName,
                    command.timestamp,
                ).apply {
                    applyModeModification(command.channelModeModification)
                    applyModeModification(command.memberModeModification)
                }
            )
        }
    }

    @Handler
    fun fmodeChanged(sender: Identifier, command: FMODE) {
        if (command.target is ChannelName) {
            val channel = Channels.get(command.target)
            if (channel.shouldBeApplied(command.timestamp)) {
                channel.applyModeModification(command.modeModification)
            }
        }
    }

    @Handler
    fun metadataChanged(sender: ServerId, command: METADATA) {
        if (command.target is ChannelName) {
            val channel = Channels.get(command.target)
            if (command.value.isNullOrBlank()) {
                channel.metadata.remove(command.type)
            } else {
                channel.metadata[command.type] = command.value
            }
        }
    }

    @Handler
    fun serverSettedTopic(sender: ServerId, command: FTOPIC) {
        val channel = Channels.get(command.channelName)
        channel.topic = Channel.Topic(command.content, command.setter, command.settedAt)
    }

    @Handler
    fun userChangedTopic(sender: UniversalUserId, command: TOPIC) {
        val channel = Channels.get(command.channelName)
        val user = Users.get(sender)
        channel.topic = Channel.Topic(command.content, user.mask, LocalDateTime.now(ZoneOffset.UTC))
    }

    @Handler
    fun memberKicked(sender: UniversalUserId, command: KICK) {
        Channels.leaveUser(command.channelName, command.targetUserId)
    }

    @Handler
    fun memberParted(sender: UniversalUserId, command: PART) {
        Channels.leaveUser(command.channelName, sender)
    }
}