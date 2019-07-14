package ru.kuchanov.scpquiz.mvp.view.util

import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface SettingsView : BaseView, AuthView {
    fun showProgress(show: Boolean)
    fun showLang(langCode: String)
    fun showLangsChooser(langs: Set<String>, lang: String)
    fun showSound(enabled: Boolean)
    fun showVibration(enabled: Boolean)
}
