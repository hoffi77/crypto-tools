package de.alijas.dirproc.zip

import de.alijas.dirproc.FileHandlerBase
import de.alijas.dirproc.IFileHandler
import de.alijas.util.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipFileHandler(tempDirPath: String) : IFileHandler, FileHandlerBase(tempDirPath) {

    override fun handle(inputFilePath: String, outputDirPath: String?, removeInputFile: Boolean): String {
        val outputFilePathCalculated = startUp(inputFilePath, outputDirPath, "zip")
        try {
            val fis = FileInputStream(inputFilePath)
            val fos = FileOutputStream(outputFilePathCalculated)
            ZipOutputStream(fos).use { zipOs ->

                // 1. Zip file
                zipOs.setLevel(9)

                // Start writing a new file entry
                zipOs.putNextEntry(ZipEntry(File(inputFilePath).name))
                var length: Int

                // create byte buffer
                val buffer = ByteArray(Constants.WRITE_BUFFER_SIZE)

                // read and write the content of the file
                while (fis.read(buffer).also { length = it } > 0) {
                    zipOs.write(buffer, 0, length)
                }
                // current file entry is written and current zip entry is closed
                zipOs.closeEntry()
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
        return "Zip"
    }
}