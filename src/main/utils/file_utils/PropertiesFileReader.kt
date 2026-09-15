package utils.file_utils

import config.ConfigLoader
import interfaces.FileLoader
import utils.Logger
import java.io.InputStream
import java.util.Properties

class PropertiesFileReader : FileLoader<Properties> {
    override fun getFileType(filePath: String): Properties {
        val path = "env/${filePath.lowercase()}.properties"
        Logger.info("Loading config: $path")
        val input: InputStream = ConfigLoader::class.java.classLoader.getResourceAsStream(path)
            ?: throw RuntimeException("File not found: $path")

        return Properties().apply {
            load(input)
        }
    }
}
