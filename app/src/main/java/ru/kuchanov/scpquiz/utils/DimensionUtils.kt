package ru.kuchanov.scpquiz.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import ru.kuchanov.scpquiz.App

object DimensionUtils {

    fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels

    fun getScreenHeight() = Resources.getSystem().displayMetrics.heightPixels

    fun dpToPx(dp: Int): Int {
        val displayMetrics = App.INSTANCE.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}