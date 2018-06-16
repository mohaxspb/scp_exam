package ru.kuchanov.scpquiz.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

object BitmapUtils {

    fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        v.draw(c)
        return Bitmap.createScaledBitmap(b, 360, 640, true)
    }
}
