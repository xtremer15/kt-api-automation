package reporter

import auth.AuthManager
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import io.qameta.allure.Allure
import utils.Logger
import utils.PayloadTransformer
import utils.Router
import java.io.File
import java.time.LocalDateTime

val reqLogKey = AttributeKey<String>("reqLog")
val startTimeKey = AttributeKey<String>("startTime")
val startTimestampKey = AttributeKey<Long>("startTimestamp")

val CustomKtorLoggingFilter = createClientPlugin("CustomKtorLoggingFilter") {
    // 1. onRequest: Pre-request setup & auto JWT injection using Router
    onRequest { request, _ ->
        val route = Router.extract(request)

        // Auto-inject fresh valid JWT token if not present
        if (!request.headers.contains("Authorization")) {
            val token = AuthManager.getValidToken()
            if (token.isNotBlank()) {
                request.header("Authorization", "Bearer $token")
            }
        }

        val reqLog = HttpLogFormatter.buildRequestLog(route.method, route.url, route.path, route.queryParams)
        Allure.addAttachment("Custom Request: ${route.method}", "text/plain", reqLog)

        request.attributes.put(reqLogKey, reqLog)
        request.attributes.put(startTimeKey, LocalDateTime.now().toString())
        request.attributes.put(startTimestampKey, System.currentTimeMillis())
    }

    // 2. onResponse: Post-response formatting & truncated Allure/File logging
    onResponse { response ->
        val statusCode = response.status.value
        val rawBody = try { response.bodyAsText() } catch (e: Exception) { "" }
        val truncatedBody = PayloadTransformer.truncateResponseBody(rawBody, maxLength = 1500)

        val startTimeMs = response.call.request.attributes.getOrNull(startTimestampKey) ?: System.currentTimeMillis()
        val durationMs = System.currentTimeMillis() - startTimeMs

        val resLog = HttpLogFormatter.buildResponseLog(statusCode, durationMs, truncatedBody)
        Allure.addAttachment("Custom Response: $statusCode", "text/plain", resLog)

        val reqLog = response.call.request.attributes.getOrNull(reqLogKey) ?: ""
        val startTime = response.call.request.attributes.getOrNull(startTimeKey) ?: ""

        val fullLog = HttpLogFormatter.buildFullLog(startTime, reqLog, resLog)
        writeToFile(fullLog)
    }

    // 3. on(Send): Central network pipeline execution & severity-based console logging via Router
    on(Send) { request ->
        val route = Router.extract(request)
        val startMs = System.currentTimeMillis()

        Logger.info("🌐 [Ktor Outgoing Pipeline] -> ${route.method} ${route.url}")

        val call = proceed(request)

        val durationMs = System.currentTimeMillis() - startMs
        val statusCode = call.response.status.value

        when (statusCode) {
            in 200..299 -> Logger.success("🟢 [Ktor Incoming Pipeline] <- $statusCode ${route.method} ${route.url} ($durationMs ms)")
            in 300..499 -> Logger.warn("🟡 [Ktor Incoming Pipeline] <- $statusCode ${route.method} ${route.url} ($durationMs ms)")
            else -> Logger.fail("🔴 [Ktor Incoming Pipeline] <- $statusCode ${route.method} ${route.url} ($durationMs ms)")
        }

        call
    }

    // 4. transformRequestBody: Sensitive data masking & payload injection via Router
    transformRequestBody { request, body, _ ->
        if (body != null) {
            val route = Router.extract(request)
            val maskedBody = PayloadTransformer.maskSensitiveData(body)
            Logger.info("📤 [Ktor Body Transform] Outgoing payload (masked) for ${route.method} ${route.path}: $maskedBody")
        }
        null
    }

    // 5. transformResponseBody: Intercept payload status & schema logging
    transformResponseBody { response, _, _ ->
        Logger.info("📥 [Ktor Body Transform] Incoming payload status: ${response.status}")
        null
    }
}

private fun writeToFile(content: String) {
    try {
        File("api-requests.log").appendText(content)
    } catch (e: Exception) {
        Logger.error("Failed to write api-requests.log", e)
    }
}
