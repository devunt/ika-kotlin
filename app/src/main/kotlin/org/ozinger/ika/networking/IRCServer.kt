package org.ozinger.ika.networking

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.command.PING
import org.ozinger.ika.command.PONG
import org.ozinger.ika.command.SERVER
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.event.PacketReceiver
import org.ozinger.ika.event.PacketSender
import java.nio.ByteBuffer

class IRCServer(
    private val id: ServerId,
    private val name: String,
    private val description: String,
    private val password: String,
) : KoinComponent {
    private val packetSender: PacketSender by inject()
    private val packetReceiver: PacketReceiver by inject()

    private lateinit var socket: Socket

    suspend fun connect(host: String, port: Int) = coroutineScope<Unit> {
        socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(host, port)

        launch { packetReceivingLoop() }
        launch { packetSendingLoop() }
    }

    suspend fun introduceMyself() {
        packetSender.sendDirect(
            SERVER(
                name = name,
                password = password,
                distance = 0,
                sid = id,
                description = description,
            )
        )
    }

    private suspend fun packetReceivingLoop() {
        val newline = '\n'.toByte()
        val reader = socket.openReadChannel()
        while (!reader.isClosedForRead) {
            val buffer = ByteBuffer.allocate(512)
            while (true) {
                when (val b = reader.readByte()) {
                    newline -> break
                    else -> buffer.put(b)
                }
            }

            val line = String(buffer.array(), 0, buffer.position() - 1, Charsets.UTF_8)
            if (line.isEmpty()) {
                continue
            }

            val packet = Serializers.decodePacketFromString(line)
            if (packet.command !is PING) {
                println(">>> $packet")
            }
            packetReceiver.put(packet)
        }
    }

    private suspend fun packetSendingLoop() {
        val writer = socket.openWriteChannel(autoFlush = true)
        while (true) {
            val packet = packetSender.get()
            if (packet.command !is PONG) {
                println("<<< $packet")
            }
            writer.writeStringUtf8(Serializers.encodePacketToString(packet) + "\r\n")
        }
    }
}