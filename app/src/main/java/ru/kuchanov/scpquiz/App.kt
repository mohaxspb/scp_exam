package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.vk.sdk.VKSdk
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppModule
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule


@SuppressWarnings("unused")
class App : MultiDexApplication() {

    companion object {
        lateinit var INSTANCE: App
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        initTimber()
        initDi()
        initYandexMetrica()
        VKSdk.initialize(this)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        //use it for printing keys hash for facebook
//        Timber.d("App#onCreate")
//        SystemUtils.printCertificateFingerprints(this)
    }

    private fun initYandexMetrica() {
        val configBuilder = YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_METRICA_API_KEY)
        YandexMetrica.activate(applicationContext, configBuilder.build())
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initTimber() = Timber.plant(Timber.DebugTree())

    private fun initDi() {
        Toothpick
                .openScope(Di.Scope.APP)
                .installModules(
                        SmoothieApplicationModule(this),
                        AppModule(this)
                )

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment())
        }
    }
}