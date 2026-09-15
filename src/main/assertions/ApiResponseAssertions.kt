package assertions

import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import models.ApiResponse

class ApiResponseAssertionScope(val response: ApiResponse) {
    fun hasStatus(expected: Int) {
        response.statusCode shouldBe expected
    }

    fun isSuccess() {
        response.isSuccess shouldBe true
    }

    fun isFast(underMs: Long = 1000L) {
        response.durationMs shouldBeLessThan underMs
    }

    fun hasHeader(name: String, expectedValue: String? = null) {
        val value = response.getHeader(name)
        if (expectedValue != null) {
            value shouldBe expectedValue
        } else {
            (value != null) shouldBe true
        }
    }

    fun bodyContains(substring: String) {
        response.bodyString shouldContain substring
    }
}

/**
 * Fluent assertion scope wrapper for ApiResponse.
 */
fun ApiResponse.verify(block: ApiResponseAssertionScope.() -> Unit): ApiResponse {
    ApiResponseAssertionScope(this).block()
    return this
}

// Infix matchers for direct assertion syntax
infix fun ApiResponse.shouldHaveStatus(expected: Int): ApiResponse {
    this.statusCode shouldBe expected
    return this
}

infix fun ApiResponse.shouldBeFastUnder(maxMs: Long): ApiResponse {
    this.durationMs shouldBeLessThan maxMs
    return this
}
