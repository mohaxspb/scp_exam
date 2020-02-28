package com.scp.scpexam.ui.utils

import com.mopub.common.MoPubReward
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubRewardedVideoListener
import timber.log.Timber

open class MyMoPubListener: MoPubInterstitial.InterstitialAdListener, MoPubRewardedVideoListener {

    override fun onInterstitialShown(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialShown")}

    override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialLoaded") }

    override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) { Timber.d("onInterstitialFailed") }

    override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialDismissed") }

    override fun onInterstitialClicked(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialClicked") }

    override fun onRewardedVideoClosed(adUnitId: String) { Timber.d("onRewardedVideoClosed") }

    override fun onRewardedVideoCompleted(adUnitIds: MutableSet<String>, reward: MoPubReward) { Timber.d("onRewardedVideoCompleted") }

    override fun onRewardedVideoPlaybackError(adUnitId: String, errorCode: MoPubErrorCode) { Timber.d("onRewardedVideoCompleted") }

    override fun onRewardedVideoLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) { Timber.d("onRewardedVideoLoadFailure") }

    override fun onRewardedVideoClicked(adUnitId: String) { Timber.d("onRewardedVideoClicked") }

    override fun onRewardedVideoStarted(adUnitId: String) { Timber.d("onRewardedVideoStarted") }

    override fun onRewardedVideoLoadSuccess(adUnitId: String) { Timber.d("onRewardedVideoLoadSuccess") }
}