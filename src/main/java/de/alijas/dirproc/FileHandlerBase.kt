package de.alijas.dirproc

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

open class FileHandlerBase(private val tempDirPath: String) {

    /**
     * Calculate output filename
     *
     * @param inputFilePath String
     * @param outputDirPath String?
     * @param addFileEnding String?
     * @return String
     */
    protected fun startUp(inputFilePath: String, outputDirPath: String?, addFileEnding: String? = null): String {
        val inputFileName = File(inputFilePath).name

        // If outputDirPath is given generate file there, else generate it to temp directory
        var outputFilePathCalculated = if (outputDirPath != null) {
            "$outputDirPath/$inputFileName"
        } else ("$tempDirPath/$inputFileName")

        outputFilePathCalculated.apply {
            if (addFileEnding != null) {
                outputFilePathCalculated += ".$addFileEnding"
            } else {
                outputFilePathCalculated = outputFilePathCalculated.substring(0, outputFilePathCalculated.length - 4)
            }
        }
        return outputFilePathCalculated
    }

    /**
     * Delete input file if configured
     *
     * @param inputFilePath String
     * @param removeInputFile Boolean
     */
    protected fun cleanUp(inputFilePath: String, removeInputFile: Boolean) {
        if (removeInputFile) {
            Files.delete(Paths.get(inputFilePath))
        }
    }
}