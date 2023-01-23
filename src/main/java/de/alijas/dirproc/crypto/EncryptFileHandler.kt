package de.alijas.dirproc.crypto

import de.alijas.dirproc.FileHandlerBase
import de.alijas.dirproc.IFileHandler
import de.alijas.util.Constants
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptFileHandler(
    tempDirPath: String,
    password: String,
    salt: ByteArray
) : IFileHandler, FileHandlerBase(tempDirPath) {
    private val secretKey: SecretKeySpec

    companion object {
        private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_SPEC_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        //private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    init {
        val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(
            /* password = */ (password + Constants.PEPPER).toCharArray(), /* salt = */
            salt, /* iterationCount = */
            65536, /* keyLength = */
            256)
        secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, KEY_SPEC_ALGORITHM)
    }

    override fun handle(inputFilePath: String, outputDirPath: String?, removeInputFile : Boolean): String {
        val outputFilePathCalculated = calculateOutputFilePath(inputFilePath, outputDirPath, "enc")
        val fis = FileInputStream(inputFilePath)
        val fos = FileOutputStream(outputFilePathCalculated)
        try {
            // Encrypt file
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))

            val inputBytes = ByteArray(Constants.WRITE_BUFFER_SIZE)
            var n = fis.read(inputBytes)
            while (n > 0) {
                val outputBytes = cipher.update(inputBytes, 0, n)
                fos.write(outputBytes)
                n = fis.read(inputBytes)
            }
            val outputBytes = cipher.doFinal()
            fos.write(outputBytes)
            fos.close()
            fis.close()
            cleanUp(inputFilePath, removeInputFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputFilePathCalculated
    }

    override fun getDescription(): String {
        return "Encrypt"
    }
}