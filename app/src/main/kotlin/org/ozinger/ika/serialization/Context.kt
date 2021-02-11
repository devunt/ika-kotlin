package org.ozinger.ika.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.polymorphic
import org.ozinger.ika.command.Command
import org.ozinger.ika.serialization.serializer.TrailingModeModificationSerializer
import org.ozinger.ika.serialization.serializer.UnknownCommandSerializer
import org.ozinger.ika.state.ModeDefinitions

object TrailingChannelModeModificationSerializer : TrailingModeModificationSerializer(ModeDefinitions::channel)
object TrailingUserModeModificationSerializer : TrailingModeModificationSerializer(ModeDefinitions::user)

var context = SerializersModule {
    polymorphic(Command::class) {
        default { UnknownCommandSerializer }
    }
}

val channelContext = context.overwriteWith(SerializersModule {
    contextual(TrailingChannelModeModificationSerializer)
})

val userContext = context.overwriteWith(SerializersModule {
    contextual(TrailingUserModeModificationSerializer)
})
