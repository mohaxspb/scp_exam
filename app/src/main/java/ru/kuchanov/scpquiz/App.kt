package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppModule
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig




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