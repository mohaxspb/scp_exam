package com.scp.scpexam.mvp.view.intro

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.mvp.BaseView

interface EnterView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgressAnimation()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgressText(text: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showImage(imageNumber: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenIntroDialogFragment()
}