package com.scp.scpexam.mvp.view.activity

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.mvp.BaseView

interface MainView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showFirstTimeAppodealAdsDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showAdsDialog(quizId: Long)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showWhyAdsDialog(quizId: Long)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showInterstitial(quizId: Long)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun startPurchase()
}