package org.ozinger.ika

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.ozinger.ika.configuration.ConfigurationLoader
import org.ozinger.ika.event.GeneratedHandlers
import org.ozinger.ika.networking.IRCServer

class Application : KoinComponent {
    private val configLoader: ConfigurationLoader by inject()
    private val config = configLoader.configuration

    suspend fun start() = coroutineScope<Unit> {
        launch { GeneratedHandlers().collect() }
        val ircServer = IRCServer(
            config.server.id,
            config.server.name,
            config.server.description,
            config.link.password,
        )
        launch { ircServer.connect(config.link.host, config.link.port) }
        ircServer.introduceMyself()
    }
}

fun main() = runBlocking<Unit> {
    startKoin {
        printLogger(level = Level.DEBUG)
        environmentProperties()
        modules(module {
            single { ConfigurationLoader(getProperty("IKA_CONF")) }
        })
    }

    val app = Application()
    app.start()
}

//    val c = TEST("hello", 3, "bad boy")
//    val s = Serializers.encodePacketToString(Packet(Sender.Server("1KA"), c))
//    println(s)
//
//    val d = Serializers.decodePacketFromString(s)
//    println(d)

//    FJOIN("hello", 1)

//    val c = CAPAB("END")
//    println(Serializers.encodeCommandToString(c))