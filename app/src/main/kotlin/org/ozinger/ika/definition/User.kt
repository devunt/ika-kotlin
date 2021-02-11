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
) {
    var operType: OperType? = null
    var away: String? = null

    val metadata = mutableMapOf<String, String>()
    val modes: Modes = mutableSetOf()

    fun shouldBeApplied(other: LocalDateTime) = timestamp >= other
}
