package services

import models.ApiResponse
import utils.BaseService

class LoginService : BaseService() {
    private const val ENDPOINT = "auth/login"

    suspend fun login(username: String, password: String, expiresInMins: Int = 30): ApiResponse {
        val payload = mapOf(
            "username" to username,
            "password" to password,
            "expiresInMins" to expiresInMins
        )
        return post(payload, ENDPOINT)
    }

    suspend fun loginWithPayload(payload: Any): ApiResponse {
        return post(payload, ENDPOINT)
    }
}
