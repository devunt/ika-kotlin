package org.ozinger.ika.handler

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.ozinger.ika.definition.Identifier
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.event.PacketReceiver
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

class HandlerProcessor : KoinComponent {
    private val handlerListProvider: HandlerListProvider by inject()
    private val packetReceiver: PacketReceiver by inject()

    private val map = mutableMapOf<Pair<SenderType, String>, MutableSet<KFunction<Unit>>>()

    init {
        for (method in handlerListProvider.list) {
            val parameters = method.valueParameters
            val senderType = when (parameters.first().type.toString()) {
                ServerId::class.qualifiedName!! -> SenderType.SERVER
                UniversalUserId::class.qualifiedName!! -> SenderType.USER
                Identifier::class.qualifiedName!! -> SenderType.BOTH
                else -> SenderType.DIRECT
            }

            val put = { senderType_: SenderType ->
                map.getOrPut(
                    Pair(senderType_, parameters.last().type.toString()),
                    ::mutableSetOf
                ).add(method)
            }
            when (senderType) {
                SenderType.BOTH -> {
                    put(SenderType.USER)
                    put(SenderType.SERVER)
                }
                else -> put(senderType)
            }
        }
    }

    enum class SenderType {
        DIRECT,
        SERVER,
        USER,
        BOTH,
    }

    suspend fun handle() {
        val packet = packetReceiver.get()
        val senderType = when (packet.sender) {
            is ServerId -> SenderType.SERVER
            is UniversalUserId -> SenderType.USER
            null -> SenderType.DIRECT
            else -> return
        }

        map[Pair(senderType, packet.command::class.qualifiedName!!)]?.forEach {
            val instance = get<AbstractHandler>(named(it.instanceParameter!!.type.toString()))
            when (senderType) {
                SenderType.DIRECT -> it.callSuspend(instance, packet.command)
                else -> it.callSuspend(instance, packet.sender, packet.command)
            }
        }
    }

    suspend fun collect() {
        while (true) {
            handle()
        }
    }
}
