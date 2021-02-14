package org.ozinger.ika.configuration

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import java.io.File

class ConfigurationLoader(private val path: String) {
    fun load(): Configuration {
        val content = File(path).readText()
        return Yaml.default.decodeFromString(content)
    }
}