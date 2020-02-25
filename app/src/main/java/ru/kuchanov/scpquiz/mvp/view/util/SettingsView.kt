package ru.kuchanov.scpquiz.mvp.view.util

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface SettingsView : BaseView, AuthView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showLang(langCode: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showLangsChooser(langs: Set<String>, lang: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showSound(enabled: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showVibration(enabled: Boolean)
}
