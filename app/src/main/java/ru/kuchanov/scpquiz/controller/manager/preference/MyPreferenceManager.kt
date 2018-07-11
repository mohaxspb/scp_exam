package ru.kuchanov.scpquiz.controller.manager.preference

import android.content.Context
import android.preference.PreferenceManager
import ru.kuchanov.scpquiz.Constants
import javax.inject.Inject

class MyPreferenceManager @Inject constructor(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setLangs(langs: Set<String>) = preferences.edit().putStringSet(Key.LANGS, langs).apply()

    fun getLangs() = preferences.getStringSet(Key.LANGS, setOf())

    fun setLang(lang: String) = preferences.edit().putString(Key.LANG, lang).apply()

    fun getLang() = preferences.getString(Key.LANG, Constants.DEFAULT_LANG)

    fun setPushToken(pushToken: String) = preferences.edit().putString(Key.PUSH_TOKEN, pushToken).apply()

    fun getPushToken() = preferences.getString(Key.PUSH_TOKEN, "")

    fun isSoundEnabled() = preferences.getBoolean(Key.SOUND_ENABLED, false)

    fun setSoundEnabled(enabled: Boolean) = preferences.edit().putBoolean(Key.SOUND_ENABLED, enabled).apply()

    fun isVibrationEnabled() = preferences.getBoolean(Key.VIBRATION_ENABLED, false)

    fun setVibrationEnabled(enabled: Boolean) = preferences.edit().putBoolean(Key.VIBRATION_ENABLED, enabled).apply()

    fun isIntroDialogShown() = preferences.getBoolean(Key.INTRO_DIALOG_SHOWN, false)

    fun setIntroDialogShown(shown: Boolean) = preferences.edit().putBoolean(Key.INTRO_DIALOG_SHOWN, shown).apply()

    //ads
    fun setNeedToShowInterstitial(show: Boolean) = preferences.edit().putBoolean(Key.NEED_TO_SHOW_INTERSTITIAL, show).apply()

    fun isNeedToShowInterstitial() = preferences.getBoolean(Key.NEED_TO_SHOW_INTERSTITIAL, false)

    fun disableAds(disable: Boolean) = preferences.edit().putBoolean(Key.ADS_DISABLED, disable).apply()

    fun isAdsDisabled() = preferences.getBoolean(Key.ADS_DISABLED, false)
}
