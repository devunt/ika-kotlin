package org.ozinger.ika.definition

import org.ozinger.ika.enumeration.OperType
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
    var isLocal: Boolean = false
    var operType: OperType? = null
    var awayReason: String? = null
    val memberOf = mutableSetOf<Member>()

    val mask get() = "$nickname!$ident@$displayedHost"
    val isOperator get() = operType != null
    val isOnAway get() = awayReason != null

    fun shouldBeApplied(other: LocalDateTime) = timestamp >= other

    override fun applyModeModification(modeModification: ModeModification) {
        modeModification.adding.forEach {
            modes.add(it)
        }

        modeModification.removing.forEach {
            modes.remove(it)
        }
    }
}
