package ru.kuchanov.scpquiz.mvp.view.monetization

import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.mvp.BaseView

interface MonetizationView : BaseView {
    fun showMonetizationActions(actions: MutableList<MyListItem>)

    fun onNeedToShowRewardedVideo()
    fun showProgress(show: Boolean)
    fun showRefreshFab(show: Boolean)
}