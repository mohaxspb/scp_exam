package com.scp.scpexam.mvp.view.game

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.controller.adapter.viewmodel.LevelViewModel
import com.scp.scpexam.mvp.BaseView

interface LevelsView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showLevels(quizes: List<LevelViewModel>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showAllLevelsFinishedPanel(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenCoins()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenSettings()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showCoins(coins: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showProgressOnQuizLevel(itemPosition: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showSwipeProgressBar(showSwipeProgressBar: Boolean)
}