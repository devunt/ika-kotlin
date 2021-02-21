package org.ozinger.ika.handler.core

import org.koin.core.component.inject
import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.CapabilityType
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.handler.AbstractHandler
import org.ozinger.ika.state.ModeDefinitionProvider
import java.time.LocalDateTime
import java.time.ZoneOffset

@Handler
class HandshakeHandler : AbstractHandler() {
    private val modeDefinitionProvider: ModeDefinitionProvider by inject()
    private val prefixPattern = Regex("""\((\p{Alpha}+?)\)""")

    @Handler
    suspend fun handshake(command: SERVER) {
        packetSender.run {
            sendAsServer(BURST(LocalDateTime.now(ZoneOffset.UTC)))
            sendAsServer(VERSION("0.1"))
            sendAsServer(ENDBURST)
        }
    }

    @Handler
    fun handleCapability(command: CAPAB) {
        if (command.type != CapabilityType.CAPABILITIES) {
            return
        }

        val capabilities =
            command.value!!.split(" ").map { it.split("=", limit = 2) }.associateBy({ it[0] }, { it[1] })
        modeDefinitionProvider.channel = ModeDefinition(capabilities["CHANMODES"]!!)
        modeDefinitionProvider.user = ModeDefinition(capabilities["USERMODES"]!!)
        modeDefinitionProvider.member = ModeDefinition(
            listOf(),
            prefixPattern.find(capabilities["PREFIX"]!!)!!.groups[1]!!.value.toList(),
            listOf(),
            listOf()
        )
    }
}
