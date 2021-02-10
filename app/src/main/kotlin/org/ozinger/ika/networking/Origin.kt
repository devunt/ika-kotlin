package org.ozinger.ika.networking

import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId

sealed class Origin {
    data class Server(val serverId: ServerId) : Origin() {
        companion object {
            val MYSELF = Server(ServerId("0ME"))
        }
    }

    data class User(val userId: UniversalUserId) : Origin()

    object Direct : Origin() {
        override fun toString() = "Direct"
    }
}
