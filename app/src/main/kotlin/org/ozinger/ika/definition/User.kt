package org.ozinger.ika.definition

import java.time.LocalDateTime

data class User(
    val id: UniversalUserId,
    var timestamp: LocalDateTime,
    var nickname: String,
    val host: String,
    var displayedHost: String,
    var ident: String,
    val ipAddress: String,
    val signonAt: LocalDateTime,
    var realname: String,
) : AbstractIRCEntity() {
    val mask: String
        get() = "$nickname!$ident@$displayedHost"

    var operType: OperType? = null
    var away: String? = null

    val joinedChannels = mutableSetOf<ChannelName>()

    fun shouldBeApplied(other: LocalDateTime) = timestamp >= other

    override fun applyModeModification(modeModification: ModeModification) {
        modeModification.adding?.forEach {
            modes.add(it)
        }

        modeModification.removing?.forEach {
            modes.remove(it)
        }
    }
}
