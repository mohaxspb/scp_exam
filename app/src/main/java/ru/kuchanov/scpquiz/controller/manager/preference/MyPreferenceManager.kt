package ru.kuchanov.scpquiz.controller.manager.preference

import android.content.Context
import androidx.preference.PreferenceManager
import ru.kuchanov.scpquiz.Constants
import javax.inject.Inject

class MyPreferenceManager @Inject constructor(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setLangs(langs: Set<String>) = preferences.edit().putStringSet(Key.LANGS, langs).apply()

    fun getLangs(): MutableSet<String>? = preferences.getStringSet(Key.LANGS, setOf())

    fun setLang(lang: String) = preferences.edit().putString(Key.LANG, lang).apply()

    fun getLang(): String = preferences.getString(Key.LANG, Constants.DEFAULT_LANG)!!

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

    fun setNeverShowAuth(neverShowAuth: Boolean) = preferences.edit().putBoolean(Key.NEVER_SHOW_AUTH, neverShowAuth).apply()

    fun getNeverShowAuth() = preferences.getBoolean(Key.NEVER_SHOW_AUTH, false)

    fun setNeverShowAdminForQuizAppAds(neverShowAdminForQuizAppAds: Boolean) = preferences.edit().putBoolean(Key.NEVER_SHOW_ADMIN_APP_ADS, neverShowAdminForQuizAppAds).apply()

    fun getNeverShowAdminForQuizAppAds() = preferences.getBoolean(Key.NEVER_SHOW_ADMIN_APP_ADS, false)

    fun getAccessToken(): String? = preferences.getString(Key.ACCESS_TOKEN, null)

    fun setAccessToken(accessToken: String?) = preferences.edit().putString(Key.ACCESS_TOKEN, accessToken).apply()

    // Access token только для получения списка квизов если Юзер не зарегистрирован
    // TrueAccessToken это реальный токен юзера

    fun getTrueAccessToken(): String? = preferences.getString(Key.TRUE_ACCESS_TOKEN, null)

    fun setTrueAccessToken(trueAccessToken: String?) = preferences.edit().putString(Key.TRUE_ACCESS_TOKEN, trueAccessToken).apply()

    fun setRefreshToken(testRefreshToken: String?) = preferences.edit().putString(Key.REFRESH_TOKEN, testRefreshToken).apply()

    fun getRefreshToken(): String? = preferences.getString(Key.REFRESH_TOKEN, null)

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

    fun setRewardedDescriptionShown(shown: Boolean) = preferences.edit().putBoolean(
            Key.APPODEAL_DESCRIPTION_SHOWN,
            shown
    ).apply()

    fun isRewardedVideoDescriptionShown() = preferences.getBoolean(Key.APPODEAL_DESCRIPTION_SHOWN, false)

    fun isPersonalDataAccepted() = preferences.getBoolean(Key.PERSONAL_DATA_ACCEPTED, false)

    fun setPersonalDataAccepted(accepted: Boolean) {
        preferences.edit().putBoolean(Key.PERSONAL_DATA_ACCEPTED, accepted).apply()
    }
}
