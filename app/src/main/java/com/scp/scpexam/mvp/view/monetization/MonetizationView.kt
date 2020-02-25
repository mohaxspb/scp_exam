package com.scp.scpexam.mvp.view.monetization

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.mvp.BaseView

interface MonetizationView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMonetizationActions(actions: MutableList<MyListItem>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToShowRewardedVideo()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showRefreshFab(show: Boolean)
}