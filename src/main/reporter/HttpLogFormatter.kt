package reporter

object HttpLogFormatter {
    fun buildRequestLog(method: String, url: String, path: String, queryParams: String): String {
        return StringBuilder().apply {
            append("Method:\n").append(method).append("\n\n")
            append("Url:\n").append(url).append("\n\n")
            append("Path:\n").append(path).append("\n\n")
            append("Query-params:\n").append(queryParams).append("\n")
        }.toString()
    }

    fun buildResponseLog(statusCode: Int, durationMs: Long, body: String): String {
        return StringBuilder().apply {
            append("Status Code:\n").append(statusCode).append("\n\n")
            append("Response Time:\n").append(durationMs).append(" ms\n\n")
            append("Body:\n").append(body).append("\n")
        }.toString()
    }

    fun buildFullLog(startTime: String, reqLog: String, resLog: String): String {
        return StringBuilder().apply {
            append("=== REQUEST ===\n")
            append("Time: ").append(startTime).append("\n")
            append(reqLog)
            append("\n=== RESPONSE ===\n")
            append(resLog)
            append("\n---------------------------------------------------\n")
        }.toString()
    }
}
