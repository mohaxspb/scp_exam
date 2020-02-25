package ru.kuchanov.scpquiz.mvp.view.game

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.mvp.BaseView

interface LevelsView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showLevels(quizes: List<LevelViewModel>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showAllLevelsFinishedPanel(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenCoins()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenSettings()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showCoins(coins: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgressOnQuizLevel(itemPosition: Int)
}