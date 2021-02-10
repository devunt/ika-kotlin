package org.ozinger.ika.configuration

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.koin.core.component.KoinComponent
import java.io.File

class ConfigurationLoader(path: String) : KoinComponent {
    val configuration: Configuration

    init {
        val content = File(path).readText()
        configuration = Yaml.default.decodeFromString(content)
    }
}