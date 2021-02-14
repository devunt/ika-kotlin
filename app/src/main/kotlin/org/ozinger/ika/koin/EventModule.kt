package org.ozinger.ika.koin

import org.koin.dsl.module
import org.ozinger.ika.event.PacketReceiver
import org.ozinger.ika.event.PacketSender
import org.ozinger.ika.handler.HandlerProcessor

val eventModule = module {
    single { PacketSender() }
    single { PacketReceiver() }
    single { HandlerProcessor() }
}