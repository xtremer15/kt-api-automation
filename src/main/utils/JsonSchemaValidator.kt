package utils

object JsonSchemaValidator {
    /**
     * Validates that a raw JSON string contains required fields or matches JSON structure.
     */
    fun validateJsonStructure(jsonString: String, requiredKeys: List<String>): Boolean {
        return try {
            val element = com.google.gson.JsonParser.parseString(jsonString)
            if (!element.isJsonObject) return false
            val obj = element.asJsonObject
            val missing = requiredKeys.filter { !obj.has(it) }
            if (missing.isNotEmpty()) {
                Logger.fail("❌ [Schema Validation] Missing required keys: $missing")
                false
            } else {
                Logger.success("✅ [Schema Validation] All required keys $requiredKeys present")
                true
            }
        } catch (e: Exception) {
            Logger.error("Failed JSON Schema parsing", e)
            false
        }
    }
}
