package de.alijas.dirproc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileProcessor(
    private val fileHandlerList: List<IFileHandler>
) {
    suspend fun processFile(inputFilePath: String, outputDirPath: String) {
        withContext(Dispatchers.IO) {
            var inPath = inputFilePath
            fileHandlerList.forEachIndexed { index, fileHandler ->
                if (index < fileHandlerList.size - 1) {
                    // First step or in between
                    inPath = fileHandler.handle(
                        inputFilePath = inPath,
                        removeInputFile = index > 0
                    )
                } else {
                    // Last step
                    fileHandler.handle(
                        inputFilePath = inPath,
                        outputDirPath = outputDirPath,
                        removeInputFile = fileHandlerList.size > 1
                    )
                }
            }
        }
    }
}