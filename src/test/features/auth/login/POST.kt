package features.auth.login

import assertions.shouldHaveStatus
import assertions.verify
import io.kotest.matchers.string.shouldNotBeBlank
import services.LoginService
import specs.BaseSpec
import specs.apiTest

class POST : BaseSpec({
    val loginService = LoginService()

    "Feature: Authentication — POST /auth/login" - {
        "Successful login with valid credentials returns JWT token" {
            apiTest("Login with valid user emilys") {
                step("Send POST /auth/login with valid credentials") {
                    val response = loginService.login(username = "emilys", password = "emilyspassword")

                    response shouldHaveStatus 200
                    response.verify {
                        isSuccess()
                        hasHeader("content-type")
                    }

                    val token = response.extract("token")
                    token?.shouldNotBeBlank()
                }
            }
        }

        "Login failure with invalid password returns 400 Bad Request" {
            apiTest("Login with invalid password") {
                step("Send POST /auth/login with wrong password") {
                    val response = loginService.login(username = "emilys", password = "wrong_password")
                    response shouldHaveStatus 400
                }
            }
        }
    }
})
