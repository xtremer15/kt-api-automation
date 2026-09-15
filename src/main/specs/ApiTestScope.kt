package specs

import io.kotest.core.test.TestScope
import utils.Logger
import utils.RequestContext

class ApiTestScope(val testScope: TestScope) {
    suspend fun step(name: String, block: suspend () -> Unit) {
        Logger.info("🔹 [Step] $name")
        block()
    }
}

/**
 * Custom Kotest test block wrapper providing step logging and automatic RequestContext cleanup.
 */
suspend fun TestScope.apiTest(name: String, block: suspend ApiTestScope.() -> Unit) {
    Logger.info("🧪 [ApiTest] Execution start: $name")
    try {
        ApiTestScope(this).block()
    } finally {
        RequestContext.reset()
    }
}
