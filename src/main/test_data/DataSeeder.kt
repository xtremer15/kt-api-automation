package test_data

import java.util.UUID
import kotlin.random.Random

object DataSeeder {
    fun seedProduct(
        title: String = "Test Product " + Random.nextInt(1000, 9999),
        price: Double = Random.nextDouble(10.0, 500.0),
        description: String = "Automated test description",
        category: String = "electronics"
    ): Map<String, Any> {
        return mapOf(
            "title" to title,
            "price" to (Math.round(price * 100.0) / 100.0),
            "description" to description,
            "category" to category
        )
    }

    fun seedUser(
        username: String = "user_" + UUID.randomUUID().toString().take(8),
        email: String = "test_${Random.nextInt(100, 999)}@example.com"
    ): Map<String, Any> {
        return mapOf(
            "username" to username,
            "email" to email,
            "password" to "Pass123!",
            "firstName" to "Test",
            "lastName" to "User"
        )
    }

    fun seedPost(
        title: String = "Post " + Random.nextInt(100, 999),
        userId: Int = 1
    ): Map<String, Any> {
        return mapOf(
            "title" to title,
            "body" to "Automated test post body content.",
            "userId" to userId,
            "tags" to listOf("testing", "automation", "kotlin")
        )
    }
}
