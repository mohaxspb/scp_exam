package ru.kuchanov.scpquiz.ui.utils

import com.appodeal.ads.RewardedVideoCallbacks
import timber.log.Timber

open class MyRewardedVideoCallbacks : RewardedVideoCallbacks {
    override fun onRewardedVideoFinished(p0: Double, p1: String?) {
        Timber.d("onRewardedVideoFinished: $p0, $p1")
        //nothing to do
    }

    override fun onRewardedVideoClosed(p0: Boolean) {
        Timber.d("onRewardedVideoClosed: $p0")
        //nothing to do
    }

    override fun onRewardedVideoLoaded(p0: Boolean) {
        //nothing to do
    }

    override fun onRewardedVideoFailedToLoad() {
        //nothing to do
    }

    override fun onRewardedVideoShown() {
        Timber.d("onRewardedVideoShown")
        //nothing to do
    }
}