package de.alijas.dirproc.base64

import de.alijas.dirproc.FileHandlerBase
import de.alijas.dirproc.IFileHandler
import de.alijas.util.Constants
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class EncodeBase64FileHandler(
    tempDirPath: String,
) : IFileHandler, FileHandlerBase(tempDirPath) {

    override fun handle(inputFilePath: String, outputDirPath: String?, removeInputFile : Boolean): String {
        val outputFilePathCalculated = calculateOutputFilePath(inputFilePath, outputDirPath, "b64")
        val fis = FileInputStream(inputFilePath)
        val fos = FileOutputStream(outputFilePathCalculated)
        try {
            val buffer = ByteArray(Constants.WRITE_BUFFER_SIZE)
            // Encode to Base64 file
            while (fis.read(buffer) >= 0) {
                fos.write(Base64.getEncoder().encode(buffer))
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
        return "Encode to Base64"
    }
}