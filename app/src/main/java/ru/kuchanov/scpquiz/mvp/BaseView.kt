package ru.kuchanov.scpquiz.mvp

import android.support.annotation.StringRes
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface BaseView: MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMessage(message: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMessage(@StringRes message: Int)
}