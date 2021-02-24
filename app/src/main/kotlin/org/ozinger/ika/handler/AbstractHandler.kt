package org.ozinger.ika.handler

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.configuration.Configuration
import org.ozinger.ika.event.PacketSender
import org.ozinger.ika.store.ChannelStore
import org.ozinger.ika.store.UserStore

abstract class AbstractHandler : KoinComponent {
    protected val configuration: Configuration by inject()
    protected val packetSender: PacketSender by inject()

    protected val userStore: UserStore by inject()
    protected val channelStore: ChannelStore by inject()
}