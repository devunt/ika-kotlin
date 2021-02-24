package org.ozinger.ika.handler.core

import org.junit.jupiter.api.Test
import org.ozinger.ika.AbstractTest
import org.ozinger.ika.command.PING
import org.ozinger.ika.command.PONG
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.networking.Packet
import strikt.api.expectThat
import strikt.assertions.containsExactly

class ConnectionHandlerTests : AbstractTest() {
    @Test
    fun pingpong() = packetTest {
        assumeAsReceived(Packet(remoteServerId, PING("123", "456"))) {
            expectThat(it)
                .containsExactly(
                    Packet(ServerId("012"), PONG("456", "123"))
                )
        }
    }
}