package org.ozinger.ika.store

import org.ozinger.ika.definition.Member
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.definition.User

class UserStore {
    private val users = mutableMapOf<UniversalUserId, User>()

    fun add(user: User) {
        require(!exists(user.id))
        users[user.id] = user
    }

    fun get(userId: String) = get(UniversalUserId(userId))
    fun get(userId: UniversalUserId) = users.getValue(userId)

    fun exists(userId: UniversalUserId) = users.containsKey(userId)

    fun iterateCopy(block: (User) -> Unit) {
        users.toMutableMap().forEach { block(it.value) }
    }

    fun del(userId: UniversalUserId) {
        get(userId).memberOf.toMutableSet().forEach(Member::leave)
        users.remove(userId)
    }
}