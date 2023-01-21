package de.alijas.dirproc

interface IFileHandler {
    fun handle(inputFilePath: String, outputDirPath: String? = null, removeInputFile : Boolean = false) : String
    fun getDescription(): String
}