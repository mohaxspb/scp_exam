package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
//        Timber.d("json: $json")
        val type = Types.newParameterizedType(List::class.java, NwQuiz::class.java)
        val adapter = moshi.adapter<List<NwQuiz>>(type)
        val quizes = adapter.fromJson(json)
//        Timber.d("quizes: $quizes")
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

        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment());
        }
    }
}