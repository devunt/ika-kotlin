package org.ozinger.ika.definition

import kotlinx.serialization.Serializable
import org.ozinger.ika.serialization.serializer.IdentifierSerializer

@Serializable(with = IdentifierSerializer::class)
sealed class Identifier(open val value: String)

@Serializable(with = IdentifierSerializer::class)
data class ServerId(override val value: String) : Identifier(value) {
    init {
        require(Regex("[0-9][A-Z0-9]{2}").matches(value))
    }
}

@Serializable(with = IdentifierSerializer::class)
data class ServerUserId(override val value: String) : Identifier(value) {
    init {
        require(Regex("[A-Z][A-Z0-9]{5}").matches(value))
    }
}

@Serializable(with = IdentifierSerializer::class)
data class UniversalUserId(override val value: String) : Identifier(value) {
    constructor(serverId: ServerId, serverUserId: ServerUserId) : this(serverId.value, serverUserId.value)
    constructor(serverId: String, serverUserId: String) : this("$serverId$serverUserId")

    val serverId = ServerId(value.take(3))
    val serverUserId = ServerUserId(value.takeLast(6))
}

@Serializable(with = IdentifierSerializer::class)
data class ChannelName(override val value: String) : Identifier(value) {
    init {
        require(value.startsWith('#'))
    }
}

@Serializable(with = IdentifierSerializer::class)
data class Wildcard(override val value: String = "*") : Identifier(value)