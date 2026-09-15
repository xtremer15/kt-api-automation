package utils

import kotlinx.coroutines.delay

object RetryUtils {
    suspend fun <T> retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 500L,
        factor: Double = 2.0,
        maxDelayMs: Long = 5000L,
        onRetry: ((attempt: Int, exception: Throwable) -> Unit)? = null,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null

        for (attempt in 1..maxAttempts) {
            try {
                return block()
            } catch (e: Throwable) {
                lastException = e
                if (attempt == maxAttempts) break

                Logger.warn("Attempt #$attempt failed (${e.message}). Retrying in ${currentDelay}ms...")
                onRetry?.invoke(attempt, e)

                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        Logger.fail("All $maxAttempts attempts failed!")
        throw lastException ?: RuntimeException("Operation failed after $maxAttempts attempts")
    }
}
