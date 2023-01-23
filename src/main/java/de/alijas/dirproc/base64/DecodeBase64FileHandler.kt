package de.alijas.dirproc.base64

import de.alijas.dirproc.FileHandlerBase
import de.alijas.dirproc.IFileHandler
import de.alijas.util.Constants
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class DecodeBase64FileHandler(
    tempDirPath: String,
) : IFileHandler, FileHandlerBase(tempDirPath) {

    override fun handle(inputFilePath: String, outputDirPath: String?, removeInputFile : Boolean): String {
        val outputFilePathCalculated = calculateOutputFilePath(inputFilePath, outputDirPath)
        val fis = FileInputStream(inputFilePath)
        val fos = FileOutputStream(outputFilePathCalculated)
        try {
            val buffer = ByteArray(Constants.WRITE_BUFFER_SIZE)
            // Decode base64 encoded file
            while (fis.read(buffer) >= 0) {
                fos.write(Base64.getDecoder().decode(buffer))
            }
            fos.close()
            fis.close()
            cleanUp(inputFilePath, removeInputFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputFilePathCalculated
    }

    override fun getDescription(): String {
        return "Decode from Base64"
    }
}