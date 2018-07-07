package ru.kuchanov.scpquiz.mvp.view.activity

import ru.kuchanov.scpquiz.mvp.BaseView

interface MainView : BaseView {

    fun showAdsDialog(quizId: Long)

    fun showWhyAdsDialog(quizId: Long)

    fun showInterstitial(quizId: Long)

    fun startPurchase()
}