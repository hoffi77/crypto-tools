package de.alijas.dirproc.crypto

import de.alijas.cmd.Base64Helper
import de.alijas.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.spec.KeySpec
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SecretFileHandler(
    password: String,
    salt: String,
    private val tempDirPath: String
) {
    private val secretKey: SecretKeySpec

    companion object {
        private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_SPEC_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        //private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    init {
        val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, 256)
        secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, KEY_SPEC_ALGORITHM)
    }

    fun encrypt(inputFile: String, outputFile: String) {
        println("Encrypt file $inputFile to $outputFile")
        try {
            FileInputStream(inputFile).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
                    val inputBytes = ByteArray(Constants.WRITE_BUFFER_SIZE)
                    var n = inputStream.read(inputBytes)
                    while (n > 0) {
                        val outputBytes = cipher.update(inputBytes, 0, n)
                        outputStream.write(outputBytes)
                        n = inputStream.read(inputBytes)
                    }
                    val outputBytes = cipher.doFinal()
                    outputStream.write(outputBytes)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun zipAndEncrypt(inputFilePath: String, outputFilePath: String) {
        withContext(Dispatchers.IO) {
            println("Zip and Encrypt file $inputFilePath to $outputFilePath")
            val tempFile = File(tempDirPath + File(inputFilePath).name)
            try {
                FileInputStream(inputFilePath).use { zipFis ->
                    FileOutputStream(tempFile).use { zipFos ->
                        ZipOutputStream(zipFos).use { zipOs ->
                            FileInputStream(tempFile).use { encFis ->
                                FileOutputStream(outputFilePath).use { encFos ->

                                    // 1. Zip file
                                    zipOs.setLevel(9)

                                    // Start writing a new file entry
                                    zipOs.putNextEntry(ZipEntry(inputFilePath))
                                    var length: Int

                                    // create byte buffer
                                    val buffer = ByteArray(Constants.WRITE_BUFFER_SIZE)

                                    // read and write the content of the file
                                    while (zipFis.read(buffer).also { length = it } > 0) {
                                        zipOs.write(buffer, 0, length)
                                    }
                                    // current file entry is written and current zip entry is closed
                                    zipOs.closeEntry()

                                    // 2. Encrypt file
                                    val cipher = Cipher.getInstance(TRANSFORMATION)
                                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
                                    val inputBytes = ByteArray(Constants.WRITE_BUFFER_SIZE)
                                    var n = encFis.read(inputBytes)
                                    while (n > 0) {
                                        val outputBytes = cipher.update(inputBytes, 0, n)
                                        encFos.write(outputBytes)
                                        n = encFis.read(inputBytes)
                                    }
                                    val outputBytes = cipher.doFinal()
                                    encFos.write(outputBytes)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tempFile.delete()
        }
    }

    suspend fun zipAndEncryptToBase64(inputFilePath: String, outputFilePath: String) {
        withContext(Dispatchers.IO) {
            println("Zip and Encrypt file $inputFilePath to $outputFilePath")
            val tempFileZip = File(tempDirPath + File(inputFilePath).name + ".zip")
            try {
                FileInputStream(inputFilePath).use { zipFis ->
                    FileOutputStream(tempFileZip).use { zipFos ->
                        ZipOutputStream(zipFos).use { zipOs ->
                            FileInputStream(tempFileZip).use { encFis ->
                                FileOutputStream(outputFilePath).use { encFos ->

                                    // 1. Zip file
                                    zipOs.setLevel(9)

                                    // Start writing a new file entry
                                    zipOs.putNextEntry(ZipEntry(inputFilePath))
                                    var length: Int

                                    // create byte buffer
                                    val buffer = ByteArray(Constants.WRITE_BUFFER_SIZE)

                                    // read and write the content of the file
                                    while (zipFis.read(buffer).also { length = it } > 0) {
                                        zipOs.write(buffer, 0, length)
                                    }
                                    // current file entry is written and current zip entry is closed
                                    zipOs.closeEntry()

                                    // 2. Encrypt file
                                    val cipher = Cipher.getInstance(TRANSFORMATION)
                                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
                                    val inputBytes = ByteArray(4096)
                                    var n = encFis.read(inputBytes)
                                    while (n > 0) {
                                        val outputBytes = cipher.update(inputBytes, 0, n)
                                        encFos.write(outputBytes)
                                        n = encFis.read(inputBytes)
                                    }
                                    val outputBytes = cipher.doFinal()
                                    encFos.write(outputBytes)
                                    encFos.close()
                                    Base64Helper.encodeFileToBase64(outputFilePath)
                                    File(outputFilePath).delete()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tempFileZip.delete()
        }
    }

    fun decrypt(inputFile: String, outputFile: String) {
        println("Decrypt file $inputFile to $outputFile")
        try {
            FileInputStream(inputFile).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
                    val buff = ByteArray(Constants.WRITE_BUFFER_SIZE)
                    var readBytes = inputStream.read(buff)
                    while (readBytes > -1) {
                        outputStream.write(cipher.update(buff, 0, readBytes))
                        readBytes = inputStream.read(buff)
                    }
                    outputStream.write(cipher.doFinal())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun decryptAndUnzip(inputFilePath: String, outputFilePath: String) {
        withContext(Dispatchers.IO) {
            println("Decrypt and unzip file $inputFilePath to $outputFilePath")
            val tempFile = File(tempDirPath + File(inputFilePath).name)
            try {
                FileInputStream(inputFilePath).use { encFis ->
                    FileOutputStream(tempFile).use { decFos ->
                        ZipInputStream(Files.newInputStream(Paths.get(tempFile.absolutePath))).use { zipFis ->
                            FileOutputStream(outputFilePath).use { zipFos ->
                                val cipher = Cipher.getInstance(TRANSFORMATION)
                                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
                                val buff = ByteArray(Constants.WRITE_BUFFER_SIZE)
                                var readBytes = encFis.read(buff)
                                while (readBytes > -1) {
                                    decFos.write(cipher.update(buff, 0, readBytes))
                                    readBytes = encFis.read(buff)
                                }
                                decFos.write(cipher.doFinal())
                                zipFis.nextEntry
                                var len: Int?
                                while (zipFis.read(buff).also { len = it } > 0) {
                                    zipFos.write(buff, 0, len!!)
                                }
                                zipFis.closeEntry()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tempFile.delete()
        }
    }

}