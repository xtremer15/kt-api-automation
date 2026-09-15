package utils.file_utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import interfaces.FileLoader
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths

class JsonFileLoader : FileLoader<JsonObject> {
    @Throws(IOException::class)
    override fun getFileType(filePath: String): JsonObject {
        val rawFile = Files.newInputStream(Paths.get(filePath))
        val fileContent = BufferedReader(InputStreamReader(rawFile)).use { reader ->
            reader.lines().toArray().joinToString("\n")
        }
        return JsonParser.parseString(fileContent).asJsonObject
    }

    fun getKey(key: String, path: String): JsonElement? {
        val file = getFileType(path)
        return file.get(key)
    }
}
