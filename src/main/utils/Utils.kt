package utils

import java.util.UUID

object Utils {
    fun generateUUID(): String = UUID.randomUUID().toString()
}
