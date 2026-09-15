package config

object TestContext {
    var envName: String? = null
    var baseUrl: String? = null
    var apiKey: String? = null

    private val authTokenThreadLocal = ThreadLocal<String>()
    private val entityIdThreadLocal = ThreadLocal<String>()

    var authToken: String?
        get() = authTokenThreadLocal.get()
        set(value) {
            if (value != null) authTokenThreadLocal.set(value) else authTokenThreadLocal.remove()
        }

    var entityId: String?
        get() = entityIdThreadLocal.get()
        set(value) {
            if (value != null) entityIdThreadLocal.set(value) else entityIdThreadLocal.remove()
        }

    fun clearThreadContext() {
        authTokenThreadLocal.remove()
        entityIdThreadLocal.remove()
    }
}
