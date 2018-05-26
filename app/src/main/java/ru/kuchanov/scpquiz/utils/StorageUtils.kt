package ru.kuchanov.scpquiz.utils

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class StorageUtils {

    companion object {
        @Throws(IOException::class)
        fun readFromAssets(context: Context, filename: String): String {
            val reader = BufferedReader(InputStreamReader(context.assets.open(filename), "UTF-8"))

            // do reading, usually loop until end of file reading
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line) // process line
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
}