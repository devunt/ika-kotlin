package org.ozinger.ika.handler.state

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.*
import org.ozinger.ika.handler.AbstractHandler
import org.ozinger.ika.state.Users
import java.time.Duration

@Handler
class UserStateHandler : AbstractHandler() {
    @Handler
    fun userConnected(sender: ServerId, command: UID) {
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
            ).apply {
                applyModeModification(command.modeModification)
            }
        )
    }

    @Handler
    fun nicknameChanged(sender: UniversalUserId, command: NICK) {
        val user = Users.get(sender)
        user.nickname = command.nickname
    }

    @Handler
    fun displayedHostChanged(sender: UniversalUserId, command: FHOST) {
        val user = Users.get(sender)
        user.displayedHost = command.displayedHost
    }

    @Handler
    fun realnameChanged(sender: UniversalUserId, command: FNAME) {
        val user = Users.get(sender)
        user.realname = command.realname
    }

    @Handler
    fun fmodeChanged(sender: Identifier, command: FMODE) {
        applyModeModifitication(command.target, command.modeModification)
    }

    @Handler
    fun modeChanged(sender: UniversalUserId, command: MODE) {
        applyModeModifitication(command.targetUserId, command.modeModification)
    }

    private fun applyModeModifitication(target: Identifier, modeModification: ModeModification) {
        if (target is UniversalUserId) {
            val user = Users.get(target)
            user.applyModeModification(modeModification)
        }
    }

    @Handler
    suspend fun nicknameChangeRequested(sender: ServerId, command: SVSNICK) {
//        if (command.targetUserId.serverId == thisServerId) {
//            val user = Users.get(command.targetUserId)
//            user.nickname = command.nickname
//            user.timestamp = command.timestamp
//            packetSender.sendAsUser(user.id, NICK(user.nickname, user.timestamp))
//        }
    }

    @Handler
    fun metadataChanged(sender: ServerId, command: METADATA) {
        if (command.target is UniversalUserId) {
            val user = Users.get(command.target)
            if (command.value.isNullOrBlank()) {
                user.metadata.remove(command.type)
            } else {
                user.metadata[command.type] = command.value
            }
        }
    }

    @Handler
    fun awayStatusChanged(sender: UniversalUserId, command: AWAY) {
        val user = Users.get(sender)
        user.away = command.reason
    }

    @Handler
    fun operatorAuthenticated(sender: UniversalUserId, command: OPERTYPE) {
        val user = Users.get(sender)
        user.operType = command.type
        user.modes.add(Mode('o'))
    }

    @Handler
    suspend fun userIdleTimeRequestd(sender: UniversalUserId, command: IDLE) {
        val user = Users.get(command.targetUserId)
        packetSender.sendAsUser(user.id, IDLE(sender, user.signonAt, Duration.ZERO))
    }

    @Handler
    fun userQuitted(sender: UniversalUserId, command: QUIT) {
        Users.del(sender)
    }

    @Handler
    fun anotherServerDisconnected(sender: ServerId, command: SQUIT) {
        Users.iterate {
            if (it.id.serverId == command.quittingServerId) {
                Users.del(it.id)
            }
        }
    }
}