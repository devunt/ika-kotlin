package org.ozinger.ika.test

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declareMock
import org.ozinger.ika.command.Command
import org.ozinger.ika.configuration.Configuration
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.event.PacketReceiver
import org.ozinger.ika.event.PacketSender
import org.ozinger.ika.handler.HandlerProcessor
import org.ozinger.ika.koin.definitionModule
import org.ozinger.ika.koin.eventModule
import org.ozinger.ika.networking.Packet

abstract class AbstractTest(vararg modules: Module) : KoinTest {
    private val handlerProcessor: HandlerProcessor by inject()
    private val configuration: Configuration by inject()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(configurationModule)
        modules(eventModule, definitionModule)
        modules(*modules)
    }

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz ->
        mockkClass(clazz)
    }

    protected fun packetTest(block: PacketTestScope.() -> Unit) {
        PacketTestScope().block()
    }

    inner class PacketTestScope {
        val sentPackets = mutableListOf<Packet>()

        init {
            declareMock<PacketSender> {
                coEvery { put(capture(sentPackets)) } just Runs
                coEvery { sendDirect(any()) } answers { invocation.originalCall() }
                coEvery { sendAsServer(any()) } answers { invocation.originalCall() }
                coEvery { sendAsUser(any(), any()) } answers { invocation.originalCall() }
                every { this@declareMock getProperty "configuration" } returns configuration
            }
        }

        fun receivedDirectly(command: Command) {
            simulate(Packet(null, command))
        }

        fun receviedFromServer(serverId: ServerId, command: Command) {
            simulate(Packet(serverId, command))
        }

        fun receviedFromUser(userId: UniversalUserId, command: Command) {
            simulate(Packet(userId, command))
        }

        fun receivedMany(vararg packets: Packet) {
            declareMock<PacketReceiver> {
                coEvery { get() } returnsMany packets.toList()
            }

            runBlocking {
                repeat(packets.size) {
                    handlerProcessor.handle()
                }
            }
        }

        private fun simulate(packet: Packet) {
            declareMock<PacketReceiver> {
                coEvery { get() } returns packet
            }

            runBlocking { handlerProcessor.handle() }
        }
    }
}