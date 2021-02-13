package org.ozinger.ika.networking

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ozinger.ika.command.PING
import org.ozinger.ika.command.PONG
import org.ozinger.ika.command.SERVER
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.event.CentralEventBus

class IRCServer(
    id: String,
    private val name: String,
    private val description: String,
    private val password: String,
) {
    private val id = ServerId(id)
    private lateinit var socket: Socket

    suspend fun connect(host: String, port: Int) = coroutineScope<Unit> {
        socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(host, port)

        launch { receivePackets() }
        launch { sendPackets() }
    }

    suspend fun introduceMyself() {
        CentralEventBus.Outgoing.send {
            sendDirect(
                SERVER(
                    name = name,
                    password = password,
                    distance = 0,
                    sid = id,
                    description = description,
                )
            )
        }
    }

    private suspend fun receivePackets() {
        val reader = socket.openReadChannel()
        while (!reader.isClosedForRead) {
            val line = reader.readUTF8Line()
            if (line.isNullOrEmpty()) continue
            val packet = Serializers.decodePacketFromString(line)
            if (packet.command !is PING) {
                println(">>> $packet")
            }
            CentralEventBus.Incoming.received(packet)
        }
    }

    private suspend fun sendPackets() {
        val writer = socket.openWriteChannel(autoFlush = true)
        while (true) {
            var packet = CentralEventBus.Outgoing.get()
            if (packet.origin == Origin.Server.MYSELF) {
                packet = Packet(Origin.Server(id), packet.command)
            }
            if (packet.command !is PONG) {
                println("<<< $packet")
            }
            writer.writeStringUtf8(Serializers.encodePacketToString(packet) + "\n")
        }
    }
}