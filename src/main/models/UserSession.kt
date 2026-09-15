package models

data class UserSession(
    val username: String,
    val token: String,
    val expiresAtMs: Long,
    val createdAt: String
)
