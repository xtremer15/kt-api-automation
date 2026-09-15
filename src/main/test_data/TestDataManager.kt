package test_data

import java.util.concurrent.ConcurrentHashMap

object TestDataManager {
    private val cache = ConcurrentHashMap<String, Any>()

    fun setCache(key: String, value: Any) {
        if (cache.putIfAbsent(key, value) != null) {
            throw IllegalArgumentException("Key '$key' already present in cache")
        }
    }

    fun getCache(key: String): Any? = cache[key]

    @Suppress("UNCHECKED_CAST")
    fun <T> getCacheAs(key: String): T? = cache[key] as? T

    fun clearCache() {
        cache.clear()
    }

    // --- Seeder Helpers ---

    fun getSeededProduct(): Map<String, Any> = DataSeeder.seedProduct()

    fun getSeededUser(): Map<String, Any> = DataSeeder.seedUser()

    fun getSeededPost(): Map<String, Any> = DataSeeder.seedPost()
}
