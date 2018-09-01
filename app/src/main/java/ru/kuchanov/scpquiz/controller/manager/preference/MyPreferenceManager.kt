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

    fun isFingerprintEnabled() = preferences.getBoolean(Key.FINGERPRINT_ENABLED, false)

    fun setFingerprintEnabled(enabled: Boolean) = preferences.edit().putBoolean(Key.FINGERPRINT_ENABLED, enabled).apply()

    fun isIntroDialogShown() = preferences.getBoolean(Key.INTRO_DIALOG_SHOWN, false)

    fun setIntroDialogShown(shown: Boolean) = preferences.edit().putBoolean(Key.INTRO_DIALOG_SHOWN, shown).apply()

    //ads
    fun setNeedToShowInterstitial(show: Boolean) = preferences.edit().putBoolean(Key.NEED_TO_SHOW_INTERSTITIAL, show).apply()

    fun isNeedToShowInterstitial() = preferences.getBoolean(Key.NEED_TO_SHOW_INTERSTITIAL, false)

    fun disableAds(disable: Boolean) = preferences.edit().putBoolean(Key.ADS_DISABLED, disable).apply()

    fun isAdsDisabled() = preferences.getBoolean(Key.ADS_DISABLED, false)

    fun getAccessToken() = preferences.getString(Key.ACCESS_TOKEN, null)

    fun setAccessToken(accessToken: String) = preferences.edit().putString(Key.ACCESS_TOKEN, accessToken).apply()

    fun isAlreadySuggestRateUs() = preferences.getBoolean(Key.SUGGEST_RATE_US, false)

    fun setAlreadySuggestRateUs(alreadySuggestRateUs: Boolean) = preferences.edit().putBoolean(
        Key.SUGGEST_RATE_US,
        alreadySuggestRateUs
    ).apply()

    fun getLastFinishedLevelsNum() = preferences.getLong(Key.LAST_FINISHED_LEVELS_NUM, 0)

    fun setLastFinishedLevelsNum(lastFinishedLevelsNum: Long) = preferences.edit().putLong(
        Key.LAST_FINISHED_LEVELS_NUM,
        lastFinishedLevelsNum
    ).apply()

    fun getUserPassword(): String? = preferences.getString(Key.USER_PASSWORD, null)

    fun setUserPassword(password: String?) = preferences.edit().putString(Key.USER_PASSWORD, password).apply()

    fun setAppodealDescriptionShown(shown: Boolean) = preferences.edit().putBoolean(
        Key.APPODEAL_DESCRIPTION_SHOWN,
        shown
    ).apply()

    fun isAppodealDescriptionShown() = preferences.getBoolean(Key.APPODEAL_DESCRIPTION_SHOWN, false)
}
