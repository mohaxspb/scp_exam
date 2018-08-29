package ru.kuchanov.scpquiz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object BitmapUtils {

    fun loadBitmapFromView(v: View): Bitmap? {
        val width = v.measuredWidth
        val height = v.measuredHeight
        if (width <= 0 || height <= 0) {
            return null
        }
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(0, 0, width, height)
        v.draw(c)
        return Bitmap.createScaledBitmap(b, 360, 640, true)
    }

    fun persistImage(context: Context, bitmap: Bitmap, name: String) {
        val filesDir = context.cacheDir
        val imageFile = File(filesDir, "$name.png")

        var os: OutputStream? = null
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        } catch (e: Exception) {
            Timber.e(e, "Error writing bitmap")
        } finally {
            os?.flush()
            os?.close()
        }
    }

    fun fileToBitmap(imagePath: String): Bitmap {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(imagePath, options)
    }
}
