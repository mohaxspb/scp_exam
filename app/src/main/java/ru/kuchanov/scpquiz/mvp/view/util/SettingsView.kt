package ru.kuchanov.scpquiz.mvp.view.util

import ru.kuchanov.scpquiz.mvp.BaseView

interface SettingsView : BaseView {
    fun showLang(langString: String)
    fun showLangsChooser(langs: Set<String>, lang: String)
    fun showSound(enabled: Boolean)
    fun showVibration(enabled: Boolean)
}