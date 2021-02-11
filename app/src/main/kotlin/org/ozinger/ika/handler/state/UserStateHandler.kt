package org.ozinger.ika.handler.state

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.Mode
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.definition.User
import org.ozinger.ika.definition.applyModification
import org.ozinger.ika.event.CentralEventBus
import org.ozinger.ika.handler.IHandler
import org.ozinger.ika.networking.Origin
import org.ozinger.ika.state.Users
import java.time.Duration

@Handler
class UserStateHandler : IHandler {
    companion object {
        @Handler
        suspend fun newUser(origin: Origin.Server, command: UID) {
            Users.add(
                User(
                    command.userId,
                    command.timestamp,
                    command.nickname,
                    command.host,
                    command.displayedHost,
                    command.ident,
                    command.ipAddress,
                    command.signonAt,
                    command.realname,
                )
            )
        }

        @Handler
        suspend fun nicknameChange(origin: Origin.User, command: NICK) {
            val user = Users.get(origin.userId)
            user.nickname = command.nickname
        }

        @Handler
        suspend fun displayedHostChange(origin: Origin.User, command: FHOST) {
            val user = Users.get(origin.userId)
            user.displayedHost = command.displayedHost
        }

        @Handler
        suspend fun realnameChange(origin: Origin.User, command: FNAME) {
            val user = Users.get(origin.userId)
            user.realname = command.realname
        }

        @Handler
        suspend fun modeChange(origin: Origin.User, command: FMODE) {
            if (command.target is UniversalUserId) {
                val user = Users.get(command.target)
                user.modes.applyModification(command.modeModification)
            }
        }

        @Handler
        suspend fun userModeChange(origin: Origin.User, command: MODE) {
            val user = Users.get(command.targetUserId)
            user.modes.applyModification(command.modeModification)
        }

        @Handler
        suspend fun serviceNickChange(origin: Origin.Server, command: SVSNICK) {
//        if (command.targetUserId.serverId == thisServerId) {
//            val user = Users.get(command.targetUserId)
//            user.nickname = command.nickname
//            user.timestamp = command.timestamp
//            CentralEventBus.Outgoing.send {
//                sendAsUser(user.id, NICK(user.nickname, user.timestamp))
//            }
//        }
        }

        @Handler
        suspend fun metadataChange(origin: Origin.User, command: METADATA) {
            val user = Users.get(origin.userId)
            if (command.value.isNullOrBlank()) {
                user.metadata.remove(command.type)
            } else {
                user.metadata[command.type] = command.value
            }
        }

        @Handler
        suspend fun awayStatusChange(origin: Origin.User, command: AWAY) {
            val user = Users.get(origin.userId)
            user.away = command.reason
        }

        @Handler
        suspend fun operatorAuth(origin: Origin.User, command: OPERTYPE) {
            val user = Users.get(origin.userId)
            user.operType = command.type
            user.modes.add(Mode('o'))
        }

        @Handler
        suspend fun idleTimeRequest(origin: Origin.User, command: IDLE) {
            val user = Users.get(command.targetUserId)
            CentralEventBus.Outgoing.send {
                sendAsUser(user.id, IDLE(origin.userId, user.signonAt, Duration.ZERO))
            }
        }

        @Handler
        suspend fun quitUser(origin: Origin.User, command: QUIT) {
            Users.del(origin.userId)
        }

        @Handler
        suspend fun serverDisconnection(origin: Origin.Server, command: SQUIT) {
            Users.iterate {
                if (it.id.serverId == command.quittingServerId) {
                    Users.del(it.id)
                }
            }
        }
    }
}