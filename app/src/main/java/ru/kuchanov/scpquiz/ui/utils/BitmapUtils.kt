package ru.kuchanov.scpquiz.ui.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View


fun loadBitmapFromView(v: View): Bitmap {
    val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    v.draw(c)
    return b
//    val b = Bitmap.createBitmap(v.layoutParams.width, v.layoutParams.height, Bitmap.Config.ARGB_8888)
//    val c = Canvas(b)
//    v.layout(v.left, v.top, v.right, v.bottom)
//    v.draw(c)
//    return b
}
