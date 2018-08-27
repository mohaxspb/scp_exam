package ru.kuchanov.scpquiz.utils

import android.content.Context
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

object StorageUtils {

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

    fun ifFileExistsInAssets(filename: String, context: Context,path:String): Boolean {
        Timber.d("%s", context.resources.assets.list(path).contains(filename))
        return context.resources.assets.list(path).contains(filename)
    }
}