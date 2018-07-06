package ru.kuchanov.scpquiz.mvp.view.activity

import ru.kuchanov.scpquiz.mvp.BaseView

interface MainView : BaseView {

    fun showAdsDialog()

    fun showWhyAdsDialog()

    fun showInterstitial()

    fun startPurchase()
}