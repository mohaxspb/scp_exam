package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.mvp.BaseView

interface SettingsView : BaseView {
    fun showLang(langString: String)
    fun showLangsChooser(langs: Set<String>)
    fun showSound(enabled: Boolean)
    fun showVibration(enabled: Boolean)
}