package org.ozinger.ika.handler.state

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.Channel
import org.ozinger.ika.definition.ChannelName
import org.ozinger.ika.definition.Identifier
import org.ozinger.ika.handler.IHandler
import org.ozinger.ika.networking.Origin
import org.ozinger.ika.state.Channels
import org.ozinger.ika.state.Users
import java.time.LocalDateTime
import java.time.ZoneOffset

@Handler
class ChannelStateHandler : IHandler {
    companion object {
        @Handler
        suspend fun createOrUpdate(origin: Origin.Server, command: FJOIN) {
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
        suspend fun fmodeChangeUser(origin: Origin.User, command: FMODE) {
            changeMode(command.target, command)
        }

        @Handler
        suspend fun fmodeChangeServer(origin: Origin.Server, command: FMODE) {
            changeMode(command.target, command)
        }

        private fun changeMode(target: Identifier, command: FMODE) {
            if (target is ChannelName) {
                val channel = Channels.get(target)
                if (channel.shouldBeApplied(command.timestamp)) {
                    channel.applyModeModification(command.modeModification)
                }
            }
        }

        @Handler
        suspend fun metadataChange(origin: Origin.Server, command: METADATA) {
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
        suspend fun topicChangedServer(origin: Origin.Server, command: FTOPIC) {
            val channel = Channels.get(command.channelName)
            channel.topic = Channel.Topic(command.content, command.setter, command.settedAt)
        }

        @Handler
        suspend fun topicChanged(origin: Origin.User, command: TOPIC) {
            val channel = Channels.get(command.channelName)
            val user = Users.get(origin.userId)
            channel.topic = Channel.Topic(command.content, user.mask, LocalDateTime.now(ZoneOffset.UTC))
        }

        @Handler
        suspend fun memberKicked(origin: Origin.User, command: KICK) {
            Channels.leaveUser(command.channelName, command.targetUserId)
        }

        @Handler
        suspend fun memberParted(origin: Origin.User, command: PART) {
            Channels.leaveUser(command.channelName, origin.userId)
        }
    }
}