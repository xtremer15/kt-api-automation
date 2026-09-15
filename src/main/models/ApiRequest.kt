package models

import io.ktor.client.request.HttpRequestBuilder

data class ApiRequest(
    val method: String,
    val url: String,
    val path: String,
    val queryParams: String,
    val scheme: String = "https",
    val authority: String = "",
    val headers: Map<String, List<String>> = emptyMap(),
    val body: Any? = null
) {
    /**
     * Case-insensitive header lookup.
     */
    fun getHeader(name: String): String? {
        val target = name.lowercase()
        return headers.entries
            .firstOrNull { it.key.lowercase() == target }
            ?.value?.firstOrNull()
    }

    val userAgent: String? get() = getHeader("user-agent")
    val acceptLanguage: String? get() = getHeader("accept-language")
    val acceptEncoding: String? get() = getHeader("accept-encoding")
    val origin: String? get() = getHeader("origin")
    val priority: String? get() = getHeader("priority")
    val contentType: String? get() = getHeader("content-type")

    companion object {
        fun from(request: HttpRequestBuilder): ApiRequest {
            val headersMap = request.headers.build().entries()
                .associate { it.key to it.value }

            val host = request.url.host
            val port = request.url.port
            val authorityStr = if (port == 80 || port == 443 || port == 0) host else "$host:$port"

            return ApiRequest(
                method = request.method.value,
                url = request.url.buildString(),
                path = request.url.encodedPath,
                queryParams = request.url.parameters.build().toString(),
                scheme = request.url.protocol.name,
                authority = authorityStr,
                headers = headersMap,
                body = request.body
            )
        }
    }
}
