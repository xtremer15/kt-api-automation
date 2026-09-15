package services

import models.ApiResponse
import utils.BaseService

class RefreshService : BaseService() {
    private const val ENDPOINT = "auth/refresh"

    suspend fun refreshToken(refreshToken: String, expiresInMins: Int = 30): ApiResponse {
        val payload = mapOf(
            "refreshToken" to refreshToken,
            "expiresInMins" to expiresInMins
        )
        return post(payload, ENDPOINT)
    }

    suspend fun refreshWithPayload(payload: Any): ApiResponse {
        return post(payload, ENDPOINT)
    }
}
