package utils.file_utils

import interfaces.FileLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class TextFileReader : FileLoader<String> {
    @Throws(IOException::class)
    override fun getFileType(filePath: String): String {
        return Files.readString(Path.of(filePath))
    }
}
