package features.auth.refresh

import assertions.shouldHaveStatus
import assertions.verify
import io.kotest.matchers.string.shouldNotBeBlank
import services.LoginService
import services.RefreshService
import specs.BaseSpec
import specs.apiTest

class POST : BaseSpec({
    val loginService = LoginService()
    val refreshService = RefreshService()

    "Feature: Session Refresh — POST /auth/refresh" - {
        "Refresh JWT token with valid refresh token" {
            apiTest("POST /auth/refresh with valid token") {
                var refreshToken = ""
                step("Authenticate user emilys to obtain refreshToken") {
                    val loginRes = loginService.login("emilys", "emilyspassword")
                    loginRes shouldHaveStatus 200
                    refreshToken = loginRes.extract("refreshToken") ?: ""
                }

                step("Request token refresh via POST /auth/refresh") {
                    val response = refreshService.refreshToken(refreshToken)
                    response shouldHaveStatus 200
                    response.verify {
                        isSuccess()
                    }

                    val newToken = response.extract("token")
                    newToken?.shouldNotBeBlank()
                }
            }
        }
    }
})
