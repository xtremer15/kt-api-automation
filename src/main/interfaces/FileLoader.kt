package interfaces

import java.io.IOException

interface FileLoader<T> {
    @Throws(IOException::class)
    fun getFileType(filePath: String): T? = null
}
