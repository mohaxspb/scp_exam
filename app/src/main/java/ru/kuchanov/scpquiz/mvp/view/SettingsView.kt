package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.mvp.BaseView

interface SettingsView : BaseView {
    fun showLang(langString: String)
}