package org.ozinger.ika.handler.core

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.ERROR
import org.ozinger.ika.command.PING
import org.ozinger.ika.command.PONG
import org.ozinger.ika.event.CentralEventBus
import org.ozinger.ika.handler.IHandler
import org.ozinger.ika.networking.Origin

@Suppress("UNUSED_PARAMETER")
@Handler
class ConnectionHandler : IHandler {
    companion object {
        @Handler
        suspend fun pingpong(origin: Origin.Server, command: PING) {
            CentralEventBus.Outgoing.send {
                sendAsServer(PONG(command.p2, command.p1))
            }
        }

        @Handler
        suspend fun directError(origin: Origin.Direct, command: ERROR) {
            throw RuntimeException("Remote server has returned an error: ${command.reason}")
        }

        @Handler
        suspend fun serverError(origin: Origin.Server, command: ERROR) {
            throw RuntimeException("Remote server has returned an error: ${command.reason}")
        }
    }
}
