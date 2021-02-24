package org.ozinger.ika.definition

import java.time.LocalDateTime

data class Channel(
    val name: ChannelName,
    var timestamp: LocalDateTime,
) : AbstractIRCEntity() {
    val members = mutableMapOf<UniversalUserId, Member>()
    var topic = Topic("", null, null)

    fun shouldBeApplied(other: LocalDateTime) = timestamp >= other

    override fun applyModeModification(modeModification: ModeModification) {
        modeModification.adding.forEach { mode ->
            when (mode) {
                is Mode -> modes.add(mode)
                is MemberMode -> members.getOrPut(mode.target, { Member(name, mode.target) }).addMode(mode)
            }
        }

        modeModification.removing.forEach { mode ->
            when (mode) {
                is Mode -> modes.remove(mode)
                is MemberMode -> members[mode.target]!!.removeMode(mode)
            }
        }
    }

    data class Topic(
        val content: String,
        val setter: String?,
        val settedAt: LocalDateTime?,
    )
}
