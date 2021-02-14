package org.ozinger.ika.handler

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.event.PacketSender

abstract class AbstractHandler : KoinComponent {
    protected val packetSender: PacketSender by inject()
}