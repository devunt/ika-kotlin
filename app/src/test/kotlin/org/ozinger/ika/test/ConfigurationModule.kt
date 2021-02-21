package org.ozinger.ika.test

import org.koin.dsl.module
import org.ozinger.ika.configuration.Configuration
import org.ozinger.ika.definition.ServerId

val configurationModule = module {
    single {
        Configuration(
            Configuration.ServerConfiguration(
                name = "name-for-test.ozinger.org",
                description = "IKA TEST CONFIGURATION",
                id = ServerId("012"),
            ),
            Configuration.LinkConfiguration(
                name = "mainnet-for-test.ozinger.org",
                host = "127.0.0.1",
                port = 7000,
                password = "p1234",
            ),
        )
    }
}


