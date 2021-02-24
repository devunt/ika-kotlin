package org.ozinger.ika.definition

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ozinger.ika.store.ChannelStore
import org.ozinger.ika.store.UserStore

data class Member(
    val channelName: ChannelName,
    val universalUserId: UniversalUserId,
) : KoinComponent {
    private val channelStore: ChannelStore by inject()
    private val userStore: UserStore by inject()

    val channel: Channel = channelStore.get(channelName)
    val user: User = userStore.get(universalUserId)

    val modes: MutableModes = mutableSetOf()

    init {
        user.memberOf.add(this)
    }

    fun addMode(mode: MemberMode) {
        modes.add(mode)
    }

    fun removeMode(mode: MemberMode) {
        modes.remove(mode)
    }

    fun leave() {
        user.memberOf.remove(this)
        channel.members.remove(universalUserId)
        if (channel.members.isEmpty()) {
            channelStore.del(channelName)
        }
    }
}
