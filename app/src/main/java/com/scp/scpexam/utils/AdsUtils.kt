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

    const val REWARD_VIDEO_AD_UNIT_ID = "636f6661a2e94e148fa3f60ade77a1c9"
    const val FULL_SCREEN_AD_UNIT_ID = "a53aa1f7d1a443c0b510eba799c230d0"
    const val BANNER_AD_UNIT_ID = "59ef16dd0b91440199a547cc022ff7ab"

    const val TEST_REWARD_VIDEO_AD_UNIT_ID = "920b6145fb1546cf8b5cf2ac34638bb7"
    const val TEST_FULL_SCREEN_AD_UNIT_ID = "24534e1901884e398f1253216226017e"
    const val TEST_BANNER_AD_UNIT_ID = "b195f8dd8ded45fe847ad89ed1d016da"
}