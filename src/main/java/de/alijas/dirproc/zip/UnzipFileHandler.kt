package de.alijas.dirproc.zip

import de.alijas.dirproc.FileHandlerBase
import de.alijas.dirproc.IFileHandler
import de.alijas.util.Constants
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class UnzipFileHandler(tempDirPath: String) : IFileHandler, FileHandlerBase(tempDirPath) {
    override fun handle(inputFilePath: String, outputDirPath: String?, removeInputFile: Boolean): String {
        val outputFilePathCalculated = calculateOutputFilePath(inputFilePath, outputDirPath)
        try {
            val fis = FileInputStream(inputFilePath)
            val fos = FileOutputStream(outputFilePathCalculated)
            // create byte buffer
            ZipInputStream(fis).use { zipFis ->
                zipFis.nextEntry
                var len: Int?
                val buffer = ByteArray(Constants.WRITE_BUFFER_SIZE)
                while (zipFis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len!!)
                }
                zipFis.closeEntry()
            }
            fos.close()
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cleanUp(inputFilePath, removeInputFile)
        return outputFilePathCalculated
    }

    override fun getDescription(): String {
        return "Unzip"
    }
}