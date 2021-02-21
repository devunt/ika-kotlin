package org.ozinger.ika.koin

import org.koin.dsl.module
import org.ozinger.ika.configuration.Configuration
import org.ozinger.ika.configuration.ConfigurationLoader

val configurationModule = module {
    single<Configuration> { ConfigurationLoader(getProperty("IKA_CONF")).load() }
}