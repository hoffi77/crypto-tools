package de.alijas.cmd

import de.alijas.logfile.FileLineParser
import de.alijas.logfile.LinePart
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class LcmCliImportLogParserCli {

    private val delimiters = listOf(
        LinePart(title = "Promotion Status", delimiter = ", Promotion Status="),
        LinePart(title = "Commit Status", delimiter = ", Commit Status=", isRequired = false),
        LinePart(title = "Dependency Status", delimiter = ", Dependency Status="),
        LinePart(title = "Resolution Status", delimiter = "); Status -> Resolution Status="),
        LinePart(title = "Description", delimiter = ","),
        LinePart(title = "CUID", delimiter = ","),
        LinePart(title = "SID", delimiter = ","),
        LinePart(title = "Type", delimiter = " ("),
        LinePart(title = "Name", delimiter = "- "),
    )

    private val fileLineParser = FileLineParser(delimiters)

    fun parseAndWriteCsv(inputFileDir: String, outputFileName: String) {
        val contents = (mutableListOf<List<String>>())
        Files.walk(Paths.get(inputFileDir))
            .filter { path ->
                Files.isRegularFile(path) && path.absolutePathString()
                    .substring(path.absolutePathString().lastIndexOf(".") + 1) == "log"
            }
            .forEach {
                val filePath = it.absolutePathString()
                println("Parsing $filePath..")
                val elements = fileLineParser.parseFile(filePath = filePath).filter { list  -> list.isNotEmpty() }
                contents.addAll(elements)
            }
        fileLineParser.toCsv(fileName = outputFileName, contents = contents)
    }
}

fun main(args: Array<String>) {
    val lcmCliImportLogParserCli = LcmCliImportLogParserCli()
    val timeInMillis = measureTimeMillis {
        if (args.isEmpty()) {
            println("Usage: ${lcmCliImportLogParserCli.javaClass.name} <input-file-dir>")
            exitProcess(1)
        }
        val inputFileDir = args[0]

        println("Files in $inputFileDir will be parsed.")
        val outputFilePath = "$inputFileDir/logresults.csv"
        lcmCliImportLogParserCli.parseAndWriteCsv(inputFileDir, outputFilePath)
        println("Output files have been witten to $outputFilePath")
    }
    with(SimpleDateFormat("mm:ss.SSS")) {
        println("Running time was: ${format(Date(timeInMillis))}")
    }
}