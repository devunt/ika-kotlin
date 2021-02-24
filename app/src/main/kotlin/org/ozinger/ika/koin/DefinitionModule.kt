package org.ozinger.ika.koin

import org.koin.dsl.module
import org.ozinger.ika.serialization.ModeDefs

val definitionModule = module {
    single { ModeDefs() }
}