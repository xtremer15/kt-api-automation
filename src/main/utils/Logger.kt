package utils

object Logger {
    private const val RESET = "\u001B[0m"
    private const val BLUE = "\u001B[34m"   // Info
    private const val GREEN = "\u001B[32m"  // Success
    private const val YELLOW = "\u001B[33m" // Warn
    private const val RED = "\u001B[31m"    // Fail / Error

    fun info(message: String) {
        println("${BLUE}ℹ️ [INFO] $message$RESET")
    }

    fun success(message: String) {
        println("${GREEN}✅ [SUCCESS] $message$RESET")
    }

    fun warn(message: String) {
        println("${YELLOW}⚠️ [WARN] $message$RESET")
    }

    fun fail(message: String) {
        println("${RED}❌ [FAIL] $message$RESET")
    }

    fun error(message: String, throwable: Throwable? = null) {
        println("${RED}💥 [ERROR] $message${throwable?.let { " - ${it.message}" } ?: ""}$RESET")
        throwable?.printStackTrace()
    }
}
