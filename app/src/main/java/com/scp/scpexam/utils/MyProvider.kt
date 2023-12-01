package com.scp.scpexam.utils

import com.vk.api.sdk.VKOkHttpProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MyProvider : VKOkHttpProvider() {
    @Volatile
    private var okHttpClient: OkHttpClient? = null

    override fun getClient(): OkHttpClient {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
//                        .addInterceptor(UserAgentInterceptor(VK.getSDKUserAgent()))
                .addInterceptor(
                    HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                        override fun log(message: String) {
                            Timber.tag("OkHttp").d(message)
                        }
                    })
                        .setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
                .build()
        }
        return okHttpClient!!
    }

    override fun updateClient(f: BuilderUpdateFunction) {
        okHttpClient = f.update(getClient().newBuilder()).build()
    }
}