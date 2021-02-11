package org.ozinger.ika.handler.core

import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.CapabilityType
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.event.CentralEventBus
import org.ozinger.ika.handler.IHandler
import org.ozinger.ika.networking.Origin
import org.ozinger.ika.state.ModeDefinitions
import java.time.LocalDateTime
import java.time.ZoneOffset

@Suppress("UNUSED_PARAMETER")
@Handler
class HandshakeHandler : IHandler {
    companion object {
        private val prefixPattern = Regex("""(\(\p{Alpha}+?\))""")

        @Handler
        suspend fun handshake(origin: Origin.Direct, command: SERVER) {
            CentralEventBus.Outgoing.send {
                sendAsServer(BURST(LocalDateTime.now(ZoneOffset.UTC)))
                sendAsServer(VERSION("0.1"))
                sendAsServer(ENDBURST)
            }
        }

        @Handler
        suspend fun capabilities(origin: Origin.Direct, command: CAPAB) {
            if (command.type != CapabilityType.CAPABILITIES) {
                return
            }

            val capabilities =
                command.value!!.split(" ").map { it.split("=", limit = 2) }.associateBy({ it[0] }, { it[1] })
            ModeDefinitions.channel = ModeDefinition(capabilities["CHANMODES"]!!)
            ModeDefinitions.user = ModeDefinition(capabilities["USERMODES"]!!)
            ModeDefinitions.member = ModeDefinition(
                listOf(),
                prefixPattern.find(capabilities["PREFIX"]!!)?.groups?.get(0)?.value?.toList() ?: listOf(),
                listOf(),
                listOf()
            )
        }
    }
}
