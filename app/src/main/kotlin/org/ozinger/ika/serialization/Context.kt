package org.ozinger.ika.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.polymorphic
import org.ozinger.ika.command.Command
import org.ozinger.ika.serialization.serializer.ChannelModeModificationSerializer
import org.ozinger.ika.serialization.serializer.UnknownCommandSerializer
import org.ozinger.ika.serialization.serializer.UserModeModificationSerializer

var context = SerializersModule {
    polymorphic(Command::class) {
        default { UnknownCommandSerializer }
    }
}

val channelContext = context.overwriteWith(SerializersModule {
    contextual(ChannelModeModificationSerializer)
})

val userContext = context.overwriteWith(SerializersModule {
    contextual(UserModeModificationSerializer)
})
