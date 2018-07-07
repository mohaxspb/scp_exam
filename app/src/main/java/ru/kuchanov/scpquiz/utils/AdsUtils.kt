package ru.kuchanov.scpquiz.utils

import com.google.android.gms.ads.AdRequest
import ru.kuchanov.scpquiz.BuildConfig

object AdsUtils {

    fun buildAdRequest(): AdRequest {
        val adRequestBuilder = AdRequest.Builder()

        if (BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        }
        return adRequestBuilder.build()
    }
}