package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import com.squareup.moshi.Moshi
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppModule
import ru.kuchanov.scpquiz.model.api.NwQuiz
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule
import javax.inject.Inject


class App : MultiDexApplication() {

    @Inject
    lateinit var moshi: Moshi

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initDi()

        val json = StorageUtils.readFromAssets(this, "baseData.json")
        val quizes = moshi.adapter(NwQuiz::class.java).fromJson(json)
        Timber.d("quizes: $quizes")
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
            Toothpick.setConfiguration(Configuration.forDevelopment());
        }
    }
}