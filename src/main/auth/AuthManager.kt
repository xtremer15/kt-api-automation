package auth

import config.TestContext
import di.FrameworkModule
import models.UserSession
import utils.Logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

object AuthManager {
    private const val SESSION_FILE_PATH = "target/user_session.json"

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var expiresAtTimestampMs: Long = 0L

    /**
     * Seeds initial user session before tests start.
     */
    fun seedUserSession(): UserSession {
        val sessionFile = File(SESSION_FILE_PATH)
        if (sessionFile.exists()) {
            try {
                val json = sessionFile.readText()
                val session = FrameworkModule.prettyGson.fromJson(json, UserSession::class.java)
                if (session != null && System.currentTimeMillis() < session.expiresAtMs - 30_000) {
                    Logger.success("🔑 [AuthManager] Loaded valid cached user session from $SESSION_FILE_PATH")
                    cachedToken = session.token
                    expiresAtTimestampMs = session.expiresAtMs
                    TestContext.authToken = session.token
                    return session
                }
            } catch (e: Exception) {
                Logger.warn("⚠️ [AuthManager] Existing session file invalid. Re-registering user...")
            }
        }

        // Simulate pre-requisite user registration & login
        val username = "auto_user_" + System.currentTimeMillis().toString().takeLast(6)
        val newToken = generateMockJwtToken(username)
        val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour

        val session = UserSession(
            username = username,
            token = newToken,
            expiresAtMs = expiresAt,
            createdAt = java.time.LocalDateTime.now().toString()
        )

        persistSession(session)
        cachedToken = newToken
        expiresAtTimestampMs = expiresAt
        TestContext.authToken = newToken

        Logger.success("✨ [AuthManager] Seeded new user '$username' & saved token to $SESSION_FILE_PATH")
        return session
    }

    /**
     * Returns a valid JWT token, automatically refreshing if expired or expiring within 30 seconds.
     */
    fun getValidToken(): String {
        if (cachedToken == null || System.currentTimeMillis() >= expiresAtTimestampMs - 30_000) {
            Logger.warn("🔄 [AuthManager] Token expired or expiring soon. Refreshing JWT token...")
            seedUserSession()
        }
        return cachedToken ?: ""
    }

    private fun persistSession(session: UserSession) {
        try {
            Files.createDirectories(Paths.get("target"))
            File(SESSION_FILE_PATH).writeText(FrameworkModule.prettyGson.toJson(session))
        } catch (e: Exception) {
            Logger.error("Failed to write user session file", e)
        }
    }

    private fun generateMockJwtToken(username: String): String {
        val header = Base64.getUrlEncoder().encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val exp = (System.currentTimeMillis() / 1000) + 3600
        val payloadJson = """{"sub":"12345","username":"$username","exp":$exp}"""
        val payload = Base64.getUrlEncoder().encodeToString(payloadJson.toByteArray())
        val signature = Base64.getUrlEncoder().encodeToString("signature_hash_bytes".toByteArray())
        return "$header.$payload.$signature"
    }
}
