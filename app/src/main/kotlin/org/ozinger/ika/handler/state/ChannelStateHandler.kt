package org.ozinger.ika.handler.state

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.FJOIN
import org.ozinger.ika.command.FMODE
import org.ozinger.ika.command.METADATA
import org.ozinger.ika.definition.Channel
import org.ozinger.ika.definition.ChannelName
import org.ozinger.ika.definition.Identifier
import org.ozinger.ika.definition.applyModification
import org.ozinger.ika.handler.IHandler
import org.ozinger.ika.networking.Origin
import org.ozinger.ika.state.Channels

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
                channel.modes.applyModification(command.modeModification)
            } else {
                Channels.add(
                    Channel(
                        command.channelName,
                        command.timestamp,
                    ).apply {
                        modes.applyModification(command.modeModification)
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
                    channel.modes.applyModification(command.modeModification)
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
    }
}