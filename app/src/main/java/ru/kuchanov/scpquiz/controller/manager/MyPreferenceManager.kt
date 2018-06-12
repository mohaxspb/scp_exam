package ru.kuchanov.scpquiz.controller.manager

import android.content.Context
import android.preference.PreferenceManager
import javax.inject.Inject

class MyPreferenceManager @Inject constructor(context: Context) {

    private val mPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setPushToken(pushToken: String) = mPreferences.edit().putString(Key.PUSH_TOKEN, pushToken).apply()

    fun getPushToken() = mPreferences.getString(Key.PUSH_TOKEN, "")
}
