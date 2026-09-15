package config

import models.Config
import utils.Logger
import utils.file_utils.FactoryFileLoader
import java.util.Properties

object ConfigLoader {
    @Volatile
    private var instance: Properties? = null

    fun getInstance(env: String): Properties {
        return instance ?: synchronized(this) {
            instance ?: (FactoryFileLoader.getReader("properties", env) as Properties).also { instance = it }
        }
    }

    fun resolve(): Config {
        val envName = System.getProperty("env", "dev")
        val props = getInstance(envName)

        fun readKey(key: String?): String {
            if (key.isNullOrBlank()) {
                Logger.warn("Property key is null or blank. Returning empty string.")
                return ""
            }
            val fromCI = System.getenv(key)
            if (!fromCI.isNullOrBlank()) return fromCI
            val fromCLI = System.getProperty(key)
            if (!fromCLI.isNullOrBlank()) return fromCLI
            return props.getProperty(key) ?: ""
        }

        return Config.builder()
            .envURL(readKey("BASE_URL"))
            .envName(readKey("ENV_NAME").ifBlank { envName })
            .logLevel(readKey("LOG_LEVEL"))
            .timeout(readKey("TIMEOUT").toDoubleOrNull() ?: 30.0)
            .apiKey(readKey("API_KEY").replace("\"", ""))
            .build()
    }
}
