package com.tt.dramatime.widget.subtitleview

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import kotlin.system.exitProcess

/**
 * @author J. David
 *
 *
 * Class that handles reading and writing files or reads from keyboard
 */
object IOClass {
    /**
     * Method to get the file name (or path relative to the directory) and file to write to
     * in the form of an array of strings where each string represents a line
     *
     * @param fileName  name of the file (or path relative to directory)
     * @param totalFile array of strings where each string represents a line in the file
     */
    fun writeFileTxt(fileName: String, totalFile: Array<String?>) {
        var file: FileWriter? = null
        var pw: PrintWriter? = null
        try {
            file = FileWriter(System.getProperty("user.dir")?.plus("/") + fileName)
            pw = PrintWriter(file)

            for (s in totalFile) pw.println(s)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                // Execute the "finally" to make sure the file is closed
                file?.close()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

            try {
                pw?.close()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    /**
     * Method to get the file name (or path relative to the directory) and file to write to
     * in the form of an array of strings where each string represents a line
     *
     * @param fileName name of the file (or path relative to directory)
     */
    fun readFileTxt(fileName: String): Array<String?> {
        var s = arrayOfNulls<String>(0)
        val direction = (System.getProperty("user.dir")?.plus("/")) + fileName

        // Try to load the file (archive)
        val archive: File
        var fr: FileReader? = null
        var br: BufferedReader? = null
        try {
            // Open the file and create BufferedReader in order to
            // reading easier (disposing the method readLine()).
            archive = File(direction)
            fr = FileReader(archive)
            br = BufferedReader(fr)

            // Reading the file
            var line: String
            while ((br.readLine().also { line = it }) != null) {
                val s2 = arrayOfNulls<String>(s.size + 1)
                var n = 0
                while (n < s.size) {
                    s2[n] = s[n]
                    n++
                }
                s2[n] = line.trim { it <= ' ' }
                s = s2
            }
        } catch (e: Exception) {
            System.err.println("File not found")
            System.exit(-1)
        } finally {
            // In the "finally" block, try to close the file and ensure
            // that it closes, otherwise, throw an exception
            try {
                fr?.close()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

            try {
                br?.close()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
        return s
    }

    /**
     * Method to read a line from the keyboard
     *
     * @return Returns the string being read
     */
    fun readTeclaDo(): String? {
        val response: String?
        try {
            val isr = InputStreamReader(System.`in`)
            val br = BufferedReader(isr)
            response = br.readLine()
        } catch (e: IOException) {
            System.err.println("Error reading keyboard, program complete")
            exitProcess(-1)
        }
        return response
    }
}
