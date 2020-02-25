package ru.kuchanov.scpquiz.mvp.view.intro

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.mvp.BaseView

interface EnterView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgressAnimation()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgressText(text: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showImage(imageNumber: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenIntroDialogFragment()
}