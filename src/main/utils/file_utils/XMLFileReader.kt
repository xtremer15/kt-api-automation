package utils.file_utils

import interfaces.FileLoader
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XMLFileReader : FileLoader<Document> {
    override fun getFileType(filePath: String): Document {
        return try {
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(File(filePath))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
