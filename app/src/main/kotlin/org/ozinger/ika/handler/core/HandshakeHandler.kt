package org.ozinger.ika.handler.core

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.BURST
import org.ozinger.ika.command.ENDBURST
import org.ozinger.ika.command.SERVER
import org.ozinger.ika.command.VERSION
import org.ozinger.ika.event.CentralEventBus
import org.ozinger.ika.handler.IHandler
import org.ozinger.ika.networking.Origin
import java.time.LocalDateTime
import java.time.ZoneOffset

@Suppress("UNUSED_PARAMETER")
@Handler
class HandshakeHandler : IHandler {
    companion object {
        @Handler
        suspend fun handshake(origin: Origin.Direct, command: SERVER) {
            CentralEventBus.Outgoing.send {
                sendAsServer(BURST(LocalDateTime.now(ZoneOffset.UTC)))
                sendAsServer(VERSION("0.1"))
                sendAsServer(ENDBURST)
            }
        }
    }
}
