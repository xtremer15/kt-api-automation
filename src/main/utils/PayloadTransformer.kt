package utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import di.FrameworkModule
import java.util.UUID

object PayloadTransformer {
    private val sensitiveKeys = setOf("password", "ssn", "apikey", "api_key", "token", "secret", "creditcard", "cardnumber")

    /**
     * Redacts sensitive keys in maps or JSON strings for safe logging.
     */
    fun maskSensitiveData(payload: Any?): String {
        if (payload == null) return ""
        val jsonString = if (payload is String) payload else FrameworkModule.gson.toJson(payload)

        return try {
            val element = JsonParser.parseString(jsonString)
            maskJsonElement(element)
            FrameworkModule.gson.toJson(element)
        } catch (e: Exception) {
            jsonString
        }
    }

    private fun maskJsonElement(element: JsonElement) {
        when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                obj.entrySet().forEach { (key, value) ->
                    if (sensitiveKeys.contains(key.lowercase())) {
                        obj.addProperty(key, "***MASKED***")
                    } else {
                        maskJsonElement(value)
                    }
                }
            }
            element.isJsonArray -> {
                element.asJsonArray.forEach { maskJsonElement(it) }
            }
        }
    }

    /**
     * Injects global metadata (correlationId, timestamp) into JSON object payloads.
     */
    fun injectGlobalMetadata(payload: Any?): Any? {
        if (payload == null) return null
        return try {
            val jsonString = if (payload is String) payload else FrameworkModule.gson.toJson(payload)
            val element = JsonParser.parseString(jsonString)
            if (element.isJsonObject) {
                val obj = element.asJsonObject
                if (!obj.has("correlationId")) {
                    obj.addProperty("correlationId", UUID.randomUUID().toString())
                }
                if (!obj.has("timestamp")) {
                    obj.addProperty("timestamp", System.currentTimeMillis())
                }
                FrameworkModule.gson.fromJson(obj, Map::class.java)
            } else {
                payload
            }
        } catch (e: Exception) {
            payload
        }
    }

    /**
     * Truncates oversized response body strings to keep logs/reports lean.
     */
    fun truncateResponseBody(body: String, maxLength: Int = 1000): String {
        if (body.length <= maxLength) return body
        return body.take(maxLength) + "\n... [TRUNCATED ${body.length - maxLength} CHARS FOR LEAN REPORTING]"
    }
}
