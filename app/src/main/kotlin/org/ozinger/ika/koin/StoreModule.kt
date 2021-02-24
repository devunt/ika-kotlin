package org.ozinger.ika.koin

import org.koin.dsl.module
import org.ozinger.ika.store.ChannelStore
import org.ozinger.ika.store.UserStore

val storeModule = module {
    single { UserStore() }
    single { ChannelStore() }
}