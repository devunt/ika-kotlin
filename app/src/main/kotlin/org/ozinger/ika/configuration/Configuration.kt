package org.ozinger.ika.configuration

import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    val server: ServerConfiguration,
    val link: LinkConfiguration,
) {
    @Serializable
    data class ServerConfiguration(
        val name: String,
        val description: String,
        val id: String,
    )

    @Serializable
    data class LinkConfiguration(
        val name: String,
        val host: String,
        val port: Int,
        val password: String,
    )
}


