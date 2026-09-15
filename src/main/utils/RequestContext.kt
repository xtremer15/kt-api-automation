package utils

import config.TestContext

object RequestContext {
    private val headersThreadLocal = ThreadLocal.withInitial { mutableMapOf<String, String>() }
    private val queryParamsThreadLocal = ThreadLocal.withInitial { mutableMapOf<String, String>() }

    fun init(baseUri: String) {
        TestContext.baseUrl = baseUri
        Logger.info("RequestContext initialized with baseUri: $baseUri")
    }

    fun addHeader(key: String, value: String) {
        headersThreadLocal.get()[key] = value
    }

    fun addHeaders(headers: Map<String, String>) {
        headersThreadLocal.get().putAll(headers)
    }

    fun setQueryParam(key: String, value: String) {
        queryParamsThreadLocal.get()[key] = value
    }

    fun setQueryParams(params: Map<String, String>) {
        queryParamsThreadLocal.get().putAll(params)
    }

    fun setBearer(jwt: String) {
        TestContext.authToken = jwt
        addHeader("Authorization", "Bearer $jwt")
    }

    fun getHeaders(): Map<String, String> = headersThreadLocal.get()

    fun getQueryParams(): Map<String, String> = queryParamsThreadLocal.get()

    fun reset() {
        headersThreadLocal.get().clear()
        queryParamsThreadLocal.get().clear()
        TestContext.authToken = null
    }
}
