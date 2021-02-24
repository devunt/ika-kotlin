package org.ozinger.ika.handler

import io.mockk.Called
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.ozinger.ika.AbstractTest
import org.ozinger.ika.command.AWAY
import org.ozinger.ika.command.ENDBURST
import org.ozinger.ika.definition.Identifier
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.networking.Packet

class PacketHandlingSystemTests : AbstractTest() {
    private val testHandler = spyk(TestHandler())
    private val command = AWAY("auto-away")

    private val module = module(override = true) {
        single<HandlerListProvider> {
            object : HandlerListProvider {
                override val list = listOf(
                    TestHandler::direct,
                    TestHandler::server,
                    TestHandler::user,
                    TestHandler::both,
                )
            }
        }
        single<AbstractHandler>(named(TestHandler::class.qualifiedName!!)) { testHandler }
    }

    @BeforeEach
    fun setUp(koin: Koin) {
        koin.loadModules(listOf(module))
    }

    @Test
    fun `can ignore undeclared handler`() = packetTest {
        assumeAsReceived(Packet(null, ENDBURST))
        verify { testHandler wasNot Called }
    }

    @Test
    fun `can handle direct sender`() = packetTest {
        assumeAsReceived(Packet(null, command))
        verify { testHandler.direct(command) }
    }

    @Test
    fun `can call server handler`() = packetTest {
        val sender = ServerId("123")

        assumeAsReceived(Packet(sender, command))
        verifyAll {
            testHandler.server(sender, command)
            testHandler.both(sender, command)
        }
    }

    @Test
    fun `can call user handler`() = packetTest {
        val sender = UniversalUserId("123ABCDEF")

        assumeAsReceived(Packet(sender, command))
        verifyAll {
            testHandler.user(sender, command)
            testHandler.both(sender, command)
        }
    }

    inner class TestHandler : AbstractHandler() {
        fun direct(command: AWAY) {
        }

        fun server(sender: ServerId, command: AWAY) {
        }

        fun user(sender: UniversalUserId, command: AWAY) {
        }

        fun both(sender: Identifier, command: AWAY) {
        }
    }
}