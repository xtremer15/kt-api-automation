package utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.InputStreamReader

object ResourcesJsonReader {
    val loginCredentials: JsonElement by lazy {
        val stream = this::class.java.classLoader.getResourceAsStream("login.json")
            ?: throw RuntimeException("login.json not found in resources")
        JsonParser.parseReader(InputStreamReader(stream))
    }
}
