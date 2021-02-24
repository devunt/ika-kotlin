package org.ozinger.ika.handler.state

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.*
import org.ozinger.ika.handler.AbstractHandler
import java.time.Duration

@Handler
class UserStateHandler : AbstractHandler() {
    @Handler
    fun userConnected(sender: ServerId, command: UID) {
        userStore.add(
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
                isLocal = command.userId.serverId == configuration.server.id
            }
        )
    }

    @Handler
    fun nicknameChanged(sender: UniversalUserId, command: NICK) {
        val user = userStore.get(sender)
        user.nickname = command.nickname
    }

    @Handler
    fun displayedHostChanged(sender: UniversalUserId, command: FHOST) {
        val user = userStore.get(sender)
        user.displayedHost = command.displayedHost
    }

    @Handler
    fun realnameChanged(sender: UniversalUserId, command: FNAME) {
        val user = userStore.get(sender)
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
            val user = userStore.get(target)
            user.applyModeModification(modeModification)
        }
    }

    @Handler
    suspend fun nicknameChangeRequested(sender: ServerId, command: SVSNICK) {
        val user = userStore.get(command.targetUserId)
        if (user.isLocal) {
            user.nickname = command.nickname
            user.timestamp = command.timestamp
            packetSender.sendAsUser(user.id, NICK(user.nickname, user.timestamp))
        }
    }

    @Handler
    fun metadataChanged(sender: ServerId, command: METADATA) {
        if (command.target is UniversalUserId) {
            val user = userStore.get(command.target)
            if (command.value.isNullOrBlank()) {
                user.metadata.remove(command.type)
            } else {
                user.metadata[command.type] = command.value
            }
        }
    }

    @Handler
    fun awayStatusChanged(sender: UniversalUserId, command: AWAY) {
        val user = userStore.get(sender)
        user.awayReason = command.reason
    }

    @Handler
    fun operatorAuthenticated(sender: UniversalUserId, command: OPERTYPE) {
        val user = userStore.get(sender)
        user.operType = command.type
        user.modes.add(Mode('o'))
    }

    @Handler
    suspend fun userIdleTimeRequested(sender: UniversalUserId, command: IDLE) {
        val user = userStore.get(command.targetUserId)
        if (user.isLocal) {
            packetSender.sendAsUser(user.id, IDLE(sender, user.signonAt, Duration.ZERO))
        }
    }

    @Handler
    fun userQuitted(sender: UniversalUserId, command: QUIT) {
        userStore.del(sender)
    }

    @Handler
    fun serverNetSplitted(sender: ServerId, command: SQUIT) {
        userStore.iterateCopy {
            if (it.id.serverId == command.quittingServerId) {
                userStore.del(it.id)
            }
        }
    }
}