package ru.kuchanov.scpquiz.utils

import android.content.res.Resources

object DimensionUtils {

    fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels

    fun getScreenHeight() = Resources.getSystem().displayMetrics.heightPixels
}