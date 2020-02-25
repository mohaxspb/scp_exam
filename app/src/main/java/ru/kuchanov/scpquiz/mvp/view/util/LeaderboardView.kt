package ru.kuchanov.scpquiz.mvp.view.util

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LeaderboardViewModel
import ru.kuchanov.scpquiz.model.api.NwUser
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface LeaderboardView : BaseView, AuthView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showLeaderboard(users: List<LeaderboardViewModel>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showUserPosition(user: NwUser, position: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showSwipeProgressBar(showSwipeProgressBar: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showBottomProgress(showBottomProgress: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun enableScrollListener(enableScrollListener: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showCurrentUserUI(showCurrentUserUI: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showCurrentUserProgressBar(showCurrentUserProgressBar: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showRetryButton(showRetryButton: Boolean)
}