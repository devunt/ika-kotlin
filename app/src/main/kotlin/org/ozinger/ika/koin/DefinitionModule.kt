package org.ozinger.ika.koin

import org.koin.dsl.module
import org.ozinger.ika.state.ModeDefinitionProvider

val definitionModule = module {
    single { ModeDefinitionProvider() }
}