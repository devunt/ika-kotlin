package org.ozinger.ika.definition

import org.ozinger.ika.state.Users
import java.time.LocalDateTime

data class Channel(
    val name: ChannelName,
    var timestamp: LocalDateTime,
) : AbstractIRCEntity() {
    val members = mutableMapOf<UniversalUserId, Modes>()
    var topic = Topic("", null, null)

    fun shouldBeApplied(other: LocalDateTime) = timestamp >= other

    override fun applyModeModification(modeModification: ModeModification) {
        modeModification.adding?.forEach {
            if (it.isMemberMode) {
                val userId = UniversalUserId(it.param!!)
                Users.get(userId).joinedChannels.add(name)
                val modes = members.getOrPut(userId, ::mutableSetOf)
                if (it.mode != ' ') {
                    modes.add(Mode(it.mode))
                }
            } else {
                modes.add(it)
            }
        }

        modeModification.removing?.forEach {
            if (it.isMemberMode) {
                val userId = UniversalUserId(it.param!!)
                members[userId]?.remove(Mode(it.mode))
            } else {
                modes.add(it)
            }
        }
    }

    data class Topic(
        val content: String,
        val setter: String?,
        val settedAt: LocalDateTime?,
    )
}
