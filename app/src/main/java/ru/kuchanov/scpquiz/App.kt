package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppModule
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule


@SuppressWarnings("unused")
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        initTimber()
        initDi()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

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

    companion object {
        lateinit var INSTANCE: App
    }
}