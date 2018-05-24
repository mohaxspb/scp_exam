package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.db.AppDatabase
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppModule
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.QuizConverter
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule
import javax.inject.Inject


class App : MultiDexApplication() {

    @Inject
    lateinit var moshi: Moshi

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var quizConverter: QuizConverter

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initDi()

        val json = StorageUtils.readFromAssets(this, "baseData.json")
//        Timber.d("json: $json")
        val type = Types.newParameterizedType(List::class.java, NwQuiz::class.java)
        val adapter = moshi.adapter<List<NwQuiz>>(type)
        val quizes = adapter.fromJson(json)!!
//        Timber.d("quizes: $quizes")

        Single.fromCallable {
            appDatabase.quizDao().insertQuizesWithQuizTranslations(
                quizConverter.convertCollection(
                    quizes,
                    quizConverter::convert
                ))
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onError = { Timber.e(it) },
                    onSuccess = { Timber.d(it.toString()) }
                )

//        Single.fromCallable { appDatabase.quizDao().getQuizWithQuizTranslations(2) }
//                .subscribeOn(Schedulers.io())
//                .subscribeBy(
//                    onError = { Timber.e(it) },
//                    onSuccess = { Timber.d("quiz: $it") }
//                )

//        appDatabase.quizDao().insertQuizesWithQuizTranslations(quizConverter.convertCollection(quizes, quizConverter::convert))
//        val quizesFromDb = appDatabase.quizDao().getQuizWithQuizTranslations(2)
//        Timber.d("quizesFromDb: $quizesFromDb")
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