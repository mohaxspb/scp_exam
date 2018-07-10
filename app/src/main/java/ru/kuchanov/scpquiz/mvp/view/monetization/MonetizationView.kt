package ru.kuchanov.scpquiz.mvp.view.monetization

import com.android.billingclient.api.SkuDetails
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.mvp.BaseView

interface MonetizationView : BaseView {
    fun showMonetizationActions(actions: MutableList<MyListItem>)

    fun onNeedToShowRewardedVideo()

    fun enableBuyButton(disableAdsInApp: SkuDetails)
}