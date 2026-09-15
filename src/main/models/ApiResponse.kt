package models

import di.FrameworkModule
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking

data class ApiResponse(
    val statusCode: Int,
    val statusText: String,
    val bodyString: String,
    val headers: Map<String, List<String>>,
    val durationMs: Long = 0L
) {
    val bodySize: Int get() = bodyString.length
    val isSuccess: Boolean get() = statusCode in 200..299

    /**
     * Case-insensitive response header lookup.
     */
    fun getHeader(name: String): String? {
        val target = name.lowercase()
        return headers.entries
            .firstOrNull { it.key.lowercase() == target }
            ?.value?.firstOrNull()
    }

    val contentType: String? get() = getHeader("content-type")
    val contentEncoding: String? get() = getHeader("content-encoding")
    val date: String? get() = getHeader("date")
    val priority: String? get() = getHeader("priority")
    val server: String? get() = getHeader("server")
    val serverTiming: String? get() = getHeader("server-timing")
    val timingAllowOrigin: String? get() = getHeader("timing-allow-origin")
    val altSvc: String? get() = getHeader("alt-svc")

    /**
     * Deserializes JSON response body into target type T.
     */
    inline fun <reified T> bodyAs(): T {
        return FrameworkModule.gson.fromJson(bodyString, T::class.java)
    }

    /**
     * Extracts property value from JSON response string by key name.
     */
    fun extract(key: String): String? {
        return try {
            val element = com.google.gson.JsonParser.parseString(bodyString)
            if (element.isJsonObject) {
                element.asJsonObject.get(key)?.asString
            } else null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun from(httpResponse: HttpResponse, durationMs: Long = 0L): ApiResponse {
            val rawBody = runBlocking {
                try {
                    httpResponse.bodyAsText()
                } catch (e: Exception) {
                    ""
                }
            }

            val headersMap = httpResponse.headers.entries()
                .associate { it.key to it.value }

            return ApiResponse(
                statusCode = httpResponse.status.value,
                statusText = httpResponse.status.description,
                bodyString = rawBody,
                headers = headersMap,
                durationMs = durationMs
            )
        }
    }
}
