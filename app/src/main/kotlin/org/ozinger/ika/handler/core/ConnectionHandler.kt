package org.ozinger.ika.handler.core

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.ERROR
import org.ozinger.ika.command.PING
import org.ozinger.ika.command.PONG
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.handler.AbstractHandler

@Handler
class ConnectionHandler : AbstractHandler() {
    @Handler
    suspend fun pingpong(sender: ServerId, command: PING) {
        packetSender.sendAsServer(PONG(command.p2, command.p1))
    }

    @Handler
    fun directError(command: ERROR) {
        throw RuntimeException("Remote server has returned an error: ${command.reason}")
    }

    @Handler
    fun serverError(sender: ServerId, command: ERROR) {
        throw RuntimeException("Remote server ${sender.value} has returned an error: ${command.reason}")
    }
}
