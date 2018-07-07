package ru.kuchanov.scpquiz.ui.utils

import com.appodeal.ads.RewardedVideoCallbacks

open class MyRewardedVideoCallbacks : RewardedVideoCallbacks {
    override fun onRewardedVideoFinished(p0: Int, p1: String?) {
        //nothing to do
    }

    override fun onRewardedVideoClosed(p0: Boolean) {
        //nothing to do
    }

    override fun onRewardedVideoLoaded() {
        //nothing to do
    }

    override fun onRewardedVideoFailedToLoad() {
        //nothing to do
    }

    override fun onRewardedVideoShown() {
        //nothing to do
    }
}