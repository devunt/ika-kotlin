package org.ozinger.ika.configuration

import kotlinx.serialization.Serializable
import org.ozinger.ika.definition.ServerId

@Serializable
data class Configuration(
    val server: ServerConfiguration,
    val link: LinkConfiguration,
) {
    @Serializable
    data class ServerConfiguration(
        val name: String,
        val description: String,
        val id: ServerId,
    )

    @Serializable
    data class LinkConfiguration(
        val name: String,
        val host: String,
        val port: Int,
        val password: String,
    )
}


