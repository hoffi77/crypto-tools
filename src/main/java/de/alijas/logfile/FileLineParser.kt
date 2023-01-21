package de.alijas.logfile

import java.io.*

data class LinePart(
    val title: String,
    val delimiter: String? = null,
    val isRequired: Boolean = true
)

class FileLineParser(private val lineParts: List<LinePart>) {

    fun parseFile(filePath: String): List<List<String>> {
        val contents = mutableListOf(listOf<String>())
        var fileName: String
        filePath.replace("\\", "/").apply {
            fileName = substring(lastIndexOf("/") + 1, length)
        }

        try {
            BufferedReader(FileReader(filePath)).use { br ->
                br.lines()
                    .forEach { line ->
                        parseLine(line).run {
                            if (isNotEmpty()) {
                                this.add(fileName)
                                contents.add(this)
                            }
                        }
                    }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return contents
    }


    fun toCsv(fileName: String, contents: List<List<String>>, delimiter: String = ";") {
        val fos = FileOutputStream(File(fileName))
        val bufferedWriter = BufferedWriter(OutputStreamWriter(fos))

        bufferedWriter.run {
            write("File")
            write(delimiter)

            lineParts.reversed().forEach {
                write(it.title)
                write(delimiter)
            }
            newLine()

            contents.forEachIndexed { _, line ->
                line.reversed().forEach { value ->
                    write(value)
                    write(delimiter)
                }
                newLine()
            }
            close()
        }

    }

    private fun parseLine(line: String): MutableList<String> {
        val values = mutableListOf<String>()
        var charIndex = line.length
        lineParts.forEach {
            val delimiterIndex = if (it.delimiter != null) {
                line.lastIndexOf(it.delimiter, charIndex-1)
            } else 0

            // value can't be found, but it is required, return empty values list
            if (delimiterIndex == -1 && it.isRequired) {
               return mutableListOf()
            }

            val value = if (delimiterIndex == -1) {
                ""
            } else line.substring(delimiterIndex + it.delimiter?.length!!, charIndex)

            values.add(value)

            charIndex = if (delimiterIndex == -1) charIndex else delimiterIndex

        }
        return values
    }
}