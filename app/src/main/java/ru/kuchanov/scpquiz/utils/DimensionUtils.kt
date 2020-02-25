package ru.kuchanov.scpquiz.utils

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import android.util.DisplayMetrics
import android.util.TypedValue
import ru.kuchanov.scpquiz.App
import ru.kuchanov.scpquiz.R

object DimensionUtils {

    fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels

    fun getScreenHeight() = Resources.getSystem().displayMetrics.heightPixels

    fun dpToPx(dp: Int): Int {
        val displayMetrics = App.INSTANCE.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun getActionBarHeight(activity: androidx.fragment.app.FragmentActivity): Int {
        val tv = TypedValue()
        return if (activity.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, activity.resources.displayMetrics)
        } else {
            0
        }
    }
}