package hooks

import auth.AuthManager
import config.TestContext
import io.kotest.core.config.AbstractProjectConfig
import models.Config
import utils.Logger
import utils.ResourceLockManager

class Hooks : AbstractProjectConfig() {
    override val parallelism = 4

    override suspend fun beforeProject() {
        Logger.info("🚀 Suite start — resolving config (Parallelism: $parallelism)...")
        val config = Config.resolve()
        TestContext.envName = config.envName
        TestContext.baseUrl = config.envURL
        TestContext.apiKey = config.apiKey
        Logger.success("Config loaded into TestContext (${config.envName}: ${config.envURL})")

        // Seed pre-requisite user session & persist JWT token
        AuthManager.seedUserSession()
    }

    override suspend fun afterProject() {
        ResourceLockManager.clear()
        Logger.success("Suite finished — cleared lock registry")
    }
}
