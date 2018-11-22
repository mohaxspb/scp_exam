package ru.kuchanov.scpquiz.mvp.view.util

import ru.kuchanov.scpquiz.mvp.BaseView

interface SettingsView : BaseView {
    fun showProgress(show: Boolean)
    fun showLang(langString: String)
    fun showLangsChooser(langs: Set<String>, lang: String)
    fun showSound(enabled: Boolean)
    fun showVibration(enabled: Boolean)
    fun showFingerprint(enabled: Boolean)
    fun showFingerprintDialog(enableFingerprintLogin: Boolean)
}