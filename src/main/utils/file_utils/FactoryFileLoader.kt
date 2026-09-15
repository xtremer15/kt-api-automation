package utils.file_utils

import java.io.IOException

object FactoryFileLoader {
    fun getReader(fileType: String, env: String): Any {
        return when (fileType.lowercase()) {
            "txt" -> {
                try {
                    TextFileReader().getFileType(fileType)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            "properties" -> PropertiesFileReader().getFileType(env)
            "json" -> {
                try {
                    JsonFileLoader().getFileType(fileType)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            "xml" -> XMLFileReader().getFileType(fileType)
            else -> throw IllegalArgumentException("Unsupported file type: $fileType")
        }
    }
}
