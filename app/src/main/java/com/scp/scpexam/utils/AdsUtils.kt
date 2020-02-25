package com.scp.scpexam.utils

import com.google.android.gms.ads.AdRequest
import com.scp.scpexam.BuildConfig

object AdsUtils {

    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_GAME_SCREEN_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_REWARDED_VIDEO_ID = "ca-app-pub-3940256099942544/5224354917"

    fun buildAdRequest(): AdRequest {
        val adRequestBuilder = AdRequest.Builder()

        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        }
        return adRequestBuilder.build()
    }
}