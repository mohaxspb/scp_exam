package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import ru.kuchanov.scpquiz.di.Di
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule


class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initDi()
    }

    private fun initTimber() {
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, tag, formatLogs(message), t)
            }

            private fun formatLogs(message: String): String {
                if (!message.startsWith("{")) {
                    return message
                }
                try {
                    return GsonBuilder().setPrettyPrinting().create().toJson(JsonParser().parse(message))
                } catch (m: JsonSyntaxException) {
                    return message
                }
            }
        })
    }

    private fun initDi() {
        Toothpick.openScope(Di.Scope.APP).installModules(SmoothieApplicationModule(this))

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment());
        }
    }
}