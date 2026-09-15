package services

import models.ApiResponse
import utils.BaseService

class MeService : BaseService() {
    private const val ENDPOINT = "auth/me"

    suspend fun getAuthenticatedUser(): ApiResponse {
        return get(ENDPOINT)
    }

    suspend fun getAuthenticatedUserWithToken(token: String): ApiResponse {
        setBearerToken(token)
        return get(ENDPOINT)
    }
}
