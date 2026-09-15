package features.auth.me

import assertions.shouldHaveStatus
import assertions.verify
import services.LoginService
import services.MeService
import specs.BaseSpec
import specs.apiTest

class GET : BaseSpec({
    val loginService = LoginService()
    val meService = MeService()

    "Feature: User Profile — GET /auth/me" - {
        "Retrieve authenticated user profile with valid Bearer token" {
            apiTest("GET /auth/me with active session token") {
                var token = ""
                step("Authenticate user emilys") {
                    val loginRes = loginService.login("emilys", "emilyspassword")
                    loginRes shouldHaveStatus 200
                    token = loginRes.extract("token") ?: ""
                }

                step("Fetch current user profile via GET /auth/me") {
                    val response = meService.getAuthenticatedUserWithToken(token)
                    response shouldHaveStatus 200
                    response.verify {
                        isSuccess()
                        bodyContains("emilys")
                    }
                }
            }
        }
    }
})
