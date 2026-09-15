package models

data class Config private constructor(
    val envURL: String,
    val envName: String,
    val logLevel: String,
    val timeout: Double,
    val apiKey: String
) {
    class Builder {
        private var envURL: String = ""
        private var envName: String = ""
        private var logLevel: String = "INFO"
        private var timeout: Double = 30.0
        private var apiKey: String = ""

        fun envURL(envURL: String) = apply { this.envURL = envURL }
        fun envName(envName: String) = apply { this.envName = envName }
        fun logLevel(logLevel: String) = apply { this.logLevel = logLevel }
        fun timeout(timeout: Double) = apply { this.timeout = timeout }
        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }

        fun build() = Config(envURL, envName, logLevel, timeout, apiKey)
    }

    companion object {
        fun builder() = Builder()
    }
}
