package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import ru.kuchanov.scpquiz.di.SCOPE_APP
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule


class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        initDi()
    }

    private fun initDi() {
        Toothpick.openScope(SCOPE_APP).installModules(SmoothieApplicationModule(this))

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment());
        }
    }
}