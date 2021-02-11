package org.ozinger.ika.definition

import java.time.LocalDateTime

data class Channel(
    val name: ChannelName,
    var timestamp: LocalDateTime,
) {
    val metadata = mutableMapOf<String, String>()
    val modes: Modes = mutableSetOf()

    fun shouldBeApplied(other: LocalDateTime) = timestamp >= other
}
