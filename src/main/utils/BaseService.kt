package utils

import config.TestContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import models.ApiResponse
import reporter.CustomKtorLoggingFilter

open class BaseService {
    protected val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(CustomKtorLoggingFilter)
    }

    // --- Thread-Local Request Spec Configurator ---
    private class RequestConfig {
        var url: String? = null
        var method: HttpMethod = HttpMethod.Get
        val headers = mutableMapOf<String, String>()
        val queryParams = mutableMapOf<String, String>()
        var body: Any? = null
        var contentType: ContentType = ContentType.Application.Json

        fun reset() {
            url = null
            method = HttpMethod.Get
            headers.clear()
            queryParams.clear()
            body = null
            contentType = ContentType.Application.Json
        }
    }

    private val requestConfigState = ThreadLocal.withInitial { RequestConfig() }

    // --- Fluent Builder Methods (Synced with RequestContext) ---

    fun setUrl(url: String): BaseService = apply {
        requestConfigState.get().url = url
    }

    fun setMethod(method: HttpMethod): BaseService = apply {
        requestConfigState.get().method = method
    }

    fun setHeader(key: String, value: String): BaseService = apply {
        requestConfigState.get().headers[key] = value
        RequestContext.addHeader(key, value)
    }

    fun setHeaders(headers: Map<String, String>): BaseService = apply {
        requestConfigState.get().headers.putAll(headers)
        RequestContext.addHeaders(headers)
    }

    fun setQueryParam(key: String, value: String): BaseService = apply {
        requestConfigState.get().queryParams[key] = value
        RequestContext.setQueryParam(key, value)
    }

    fun setQueryParams(params: Map<String, String>): BaseService = apply {
        requestConfigState.get().queryParams.putAll(params)
        RequestContext.setQueryParams(params)
    }

    fun setBody(body: Any): BaseService = apply {
        requestConfigState.get().body = body
    }

    fun setContentType(contentType: ContentType): BaseService = apply {
        requestConfigState.get().contentType = contentType
    }

    fun setBearerToken(token: String): BaseService = apply {
        setHeader("Authorization", "Bearer $token")
        RequestContext.setBearer(token)
    }

    protected fun buildUrl(path: String): String {
        val base = TestContext.baseUrl.ifBlank { "https://dummyjson.com" }
        return if (path.startsWith("http")) path else "$base/$path".replace("//", "/").replace(":/", "://")
    }

    // --- Execution Methods ---

    suspend fun execute(path: String = ""): ApiResponse {
        val config = requestConfigState.get()
        val finalUrl = buildUrl(config.url ?: path)

        // Merge RequestContext thread-local state with RequestConfig state
        val effectiveHeaders = RequestContext.getHeaders() + config.headers
        val effectiveQueryParams = RequestContext.getQueryParams() + config.queryParams

        val startMs = System.currentTimeMillis()

        return try {
            val httpResponse = client.request(finalUrl) {
                this.method = config.method
                contentType(config.contentType)

                effectiveHeaders.forEach { (k, v) -> header(k, v) }
                effectiveQueryParams.forEach { (k, v) -> parameter(k, v) }

                TestContext.authToken?.let {
                    if (!effectiveHeaders.containsKey("Authorization")) {
                        header("Authorization", "Bearer $it")
                    }
                }

                config.body?.let { setBody(it) }
            }

            val durationMs = System.currentTimeMillis() - startMs
            ApiResponse.from(httpResponse, durationMs)
        } finally {
            config.reset()
        }
    }

    // --- Direct Shorthand Methods ---

    suspend fun get(path: String = ""): ApiResponse {
        setMethod(HttpMethod.Get)
        return execute(path)
    }

    suspend fun post(body: Any? = null, path: String = ""): ApiResponse {
        setMethod(HttpMethod.Post)
        body?.let { setBody(it) }
        return execute(path)
    }

    suspend fun patch(body: Any? = null, path: String = ""): ApiResponse {
        setMethod(HttpMethod.Patch)
        body?.let { setBody(it) }
        return execute(path)
    }

    suspend fun put(body: Any? = null, path: String = ""): ApiResponse {
        setMethod(HttpMethod.Put)
        body?.let { setBody(it) }
        return execute(path)
    }

    suspend fun delete(path: String = ""): ApiResponse {
        setMethod(HttpMethod.Delete)
        return execute(path)
    }
}
