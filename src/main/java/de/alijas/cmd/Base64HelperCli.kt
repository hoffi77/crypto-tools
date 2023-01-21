package de.alijas.cmd

import de.alijas.util.Constants
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis


object Base64Helper {
    fun encodeFileToBase64(inputFilePath: String) {
        try {
            val outputFilePath = "$inputFilePath.b64"
            println("Base64 Encoding $inputFilePath to $outputFilePath")
            val fileContent = ByteArray(Constants.WRITE_BUFFER_SIZE)
            FileOutputStream(outputFilePath).use { fos ->
                FileInputStream(inputFilePath).use { fin ->
                    while (fin.read(fileContent) >= 0) {
                        fos.write(Base64.getEncoder().encode(fileContent))
                    }
                    fin.close()
                }
                fos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun decodeFileFromBase64(inputFilePath: String) {
        return try {
            val outputFilePath = inputFilePath.substring(0, inputFilePath.length - 4)
            println("Base64 Encoding $inputFilePath to $outputFilePath")
            val fileContent = ByteArray(Constants.WRITE_BUFFER_SIZE)
            FileOutputStream(outputFilePath).use { fos ->
                FileInputStream(inputFilePath).use { fin ->
                    while (fin.read(fileContent) >= 0) {
                        fos.write(Base64.getDecoder().decode(fileContent))
                    }
                    fin.close()
                }
                fos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun main(args: Array<String>) {
    val timeInMillis = measureTimeMillis {
        if (args.size < 2) {
            println("Usage: Base64Helper <mode> <input-file-path>")
            exitProcess(1)
        }
        val mode = args[0]
        val inputFilePath = args[1]

        when (mode) {
            "e" -> Base64Helper.encodeFileToBase64(inputFilePath)
            "d" -> Base64Helper.decodeFileFromBase64(inputFilePath)
            else -> {
                System.err.println("Mode-Parameter must either be 'e' or 'd'.")
                System.exit(1)
            }
        }
    }
    with(SimpleDateFormat("mm:ss.SSS")) {
        println("Running time was: ${format(Date(timeInMillis))}")
    }
}