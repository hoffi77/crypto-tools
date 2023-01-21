package de.alijas.cmd

import de.alijas.dirproc.IFileHandler
import de.alijas.dirproc.base64.DecodeBase64FileHandler
import de.alijas.dirproc.base64.EncodeBase64FileHandler
import de.alijas.dirproc.FileProcessor
import de.alijas.dirproc.crypto.DecryptFileHandler
import de.alijas.dirproc.crypto.EncryptFileHandler
import de.alijas.dirproc.crypto.WorkerPool
import de.alijas.dirproc.zip.UnzipFileHandler
import de.alijas.dirproc.zip.ZipFileHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class DirectoryProcessorCli(
    private val inputDirPath: String,
    private val outputDirPath: String
) {
    private lateinit var pool: WorkerPool<FileProcessor>
    private var fileHandlerList = mutableListOf<IFileHandler>()

    fun create(operationIds: String, tempDirPath: String, password: String?, salt: ByteArray) {

        // Build list of file handlers based on declared operations
        operationIds.toCharArray().forEach {
            when (it) {
                'z' -> fileHandlerList.add(ZipFileHandler(tempDirPath = tempDirPath))
                'u' -> fileHandlerList.add(UnzipFileHandler(tempDirPath = tempDirPath))
                'c' -> {
                    password?.let {
                        fileHandlerList.add(EncryptFileHandler(tempDirPath = tempDirPath, password, salt))
                    }?: run {
                        println("'password' argument missing for encryption operation!")
                        exitProcess(1)
                    }
                }
                'x' -> {
                    password?.let {
                        fileHandlerList.add(DecryptFileHandler(tempDirPath = tempDirPath, password, salt))
                    }?: run {
                        println("'password' argument missing for decryption operation!")
                        exitProcess(1)
                    }
                }
                'e' -> fileHandlerList.add(EncodeBase64FileHandler(tempDirPath = tempDirPath))
                'd' -> fileHandlerList.add(DecodeBase64FileHandler(tempDirPath = tempDirPath))
                else -> {
                    print("\"$it\" is no valid operation id!")
                    exitProcess(1)
                }
            }
        }

        // Create worker pool of SecretFileHandler's
        // Assume there is hyper threading activated: Devide virtual cores by 2
        val threadCount: Int = Runtime.getRuntime().availableProcessors() / 2

        pool = WorkerPool(
            FileProcessor(fileHandlerList)
        ).apply {
            for (i in 2..threadCount) {
                add(FileProcessor(fileHandlerList))
            }
        }

        println("-- Using TEMP dir $tempDirPath [Make sure there is enough space]")
        println("-- Using $threadCount threads.")
        println("-- Using Operations '$operationIds'")
        fileHandlerList.forEachIndexed { index, fileHandler ->
            println("    ${index + 1}. ${fileHandler.getDescription()}")
        }
        println()
    }

    @Throws(IOException::class)
    fun process() {
        println(
            """----------------------------------------------------------------------------
Process files from $inputDirPath
                to $outputDirPath
----------------------------------------------------------------------------
"""
        )

        // Create new dir's
        val dirs = Files.find(Paths.get(inputDirPath),
            999, { _: Path?, bfa: BasicFileAttributes -> bfa.isDirectory })
            .distinct()
        run {
            val it = dirs.iterator()
            while (it.hasNext()) {
                val p = it.next()
                val srcPath = p.toFile().absolutePath
                val destPath = outputDirPath + srcPath.substring(inputDirPath.length)
                if (!Files.exists(Paths.get(destPath))) {
                    File(destPath).mkdirs()
                }
            }
        }

        // travers files
        runBlocking {
            val files =
                withContext(Dispatchers.IO) {
                    Files.find(
                        Paths.get(inputDirPath),
                        999,
                        { _: Path?, bfa: BasicFileAttributes -> bfa.isRegularFile })
                }
                    .distinct()
            files.forEach { path ->
                val srcPath = path.toFile().absolutePath
                var destPath = outputDirPath + srcPath.substring(inputDirPath.length)
                destPath = destPath.substring(0, destPath.lastIndexOf(File.separator))
                launch {
                    pool.borrow { fileProzessor ->
                        println("Working $srcPath")
                        fileProzessor.processFile(srcPath, destPath)
                    }
                }
            }
        }
    }
}


fun main(args: Array<String>) {
    val version = Package.getPackages().first {
        it.name == "de.alijas.cmd"
    }.implementationVersion ?: "WithinIDE"
    println("# DirectoryProcessorCli v$version\n")

    val timeInMillis = measureTimeMillis {
        if (args.size < 3) {
            println("Usage: DirectoryProcessorCli <operation id's> <input-dir> <output-dir> <optional: password> <optional: path to keyfile>")
            println(" Valid <operation id's> are:")
            println("  z : Zip file")
            println("  u : Unzip file")
            println("  c : Encrypt file")
            println("  x : Decrypt file")
            println("  e : Encode to Base64 file")
            println("  d : Decode from Base64 file")
            println(" Put them together, e.g. 'zc' for zipping and encrypting the files in given <input-dir>")
            exitProcess(1)
        }
        val operationIds = args[0]
        val inputFilePath = args[1]
        val outputFilePath = args[2]
        val password: String? = if (args.size > 3) {args[3]} else null
        val keyfilePath: String? = if (args.size > 4) {args[4]} else null

        if (inputFilePath == outputFilePath) {
            println("<input-dir> and <output-dir> must not be the same!")
            exitProcess(1)
        }

        val tempDirPath = Paths.get(".").absolutePathString()
        val salt = if (keyfilePath != null) FileInputStream(keyfilePath).readNBytes(8) else "asdk asd#äadö a+üsd a".toByteArray()

        val cli = DirectoryProcessorCli(inputFilePath, outputFilePath)
        cli.create(operationIds, tempDirPath, password, salt)
        cli.process()
    }
    with(SimpleDateFormat("mm:ss.SSS")) {
        println("\nRunning time was: ${format(Date(timeInMillis))}")
    }
}