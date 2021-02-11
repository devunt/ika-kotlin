package org.ozinger.ika.state

import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.definition.User

object Users {
    private val users = mutableMapOf<UniversalUserId, User>()

    fun add(user: User) {
        users[user.id] = user
    }

    fun get(userId: UniversalUserId) = users.getValue(userId)

    fun iterate(block: (User) -> Unit) {
        users.forEach { block(it.value) }
    }

    fun del(userId: UniversalUserId) {
        get(userId).joinedChannels.toMutableList().forEach {
            Channels.leaveUser(it, userId)
        }
        users.remove(userId)
    }
}