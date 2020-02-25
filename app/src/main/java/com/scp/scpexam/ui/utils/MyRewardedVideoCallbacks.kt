package com.scp.scpexam.ui.utils

import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import timber.log.Timber

open class MyRewardedVideoCallbacks : RewardedVideoAdListener {

    override fun onRewardedVideoAdClosed() {
        Timber.d("onRewardedVideoAdClosed")
    }

    override fun onRewardedVideoAdLeftApplication() {
        Timber.d("onRewardedVideoAdLeftApplication")
    }

    override fun onRewardedVideoAdLoaded() {
        Timber.d("onRewardedVideoAdLoaded")
    }

    override fun onRewardedVideoAdOpened() {
        Timber.d("onRewardedVideoAdOpened")
    }

    override fun onRewardedVideoCompleted() {
        Timber.d("onRewardedVideoCompleted")
    }

    override fun onRewarded(rewardItem: RewardItem?) {
        Timber.d("onRewardedVideoAdFailedToLoad: $rewardItem")
    }

    override fun onRewardedVideoStarted() {
        Timber.d("onRewardedVideoStarted")
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        Timber.d("onRewardedVideoAdFailedToLoad: $errorCode")
    }
}