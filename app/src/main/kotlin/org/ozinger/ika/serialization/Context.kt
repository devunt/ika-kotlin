package org.ozinger.ika.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.ozinger.ika.command.Command
import org.ozinger.ika.serialization.serializer.UnknownCommandSerializer

val context = SerializersModule {
    polymorphic(Command::class) {
        default { UnknownCommandSerializer }
    }
}

