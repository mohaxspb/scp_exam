package com.scp.scpexam.mvp.view.monetization

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.mvp.AuthView
import com.scp.scpexam.mvp.BaseView

interface MonetizationView : BaseView, AuthView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showMonetizationActions(actions: MutableList<MyListItem>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToShowRewardedVideo()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showRefreshFab(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun scrollToTop()

}