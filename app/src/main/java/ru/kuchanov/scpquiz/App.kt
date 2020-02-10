package ru.kuchanov.scpquiz

import android.support.multidex.MultiDexApplication
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.vk.sdk.VKSdk
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppModule
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import ru.kuchanov.scpquiz.model.db.UserRole
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule
import javax.inject.Inject


@SuppressWarnings("unused")
class App : MultiDexApplication() {

    companion object {
        lateinit var INSTANCE: App
    }

    @Inject
    lateinit var transactionInteractor: TransactionInteractor

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        initTimber()
        initDi()
        initYandexMetrica()
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
        VKSdk.initialize(this)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        initScore()

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

    private fun initScore() {
        Completable.fromCallable {
            if (appDatabase.transactionDao().getOneByTypeNoReactive(TransactionType.UPDATE_SYNC) == null) {
                val quizTransaction = QuizTransaction(
                        quizId = null,
                        transactionType = TransactionType.UPDATE_SYNC,
                        coinsAmount = appDatabase.userDao().getOneByRoleSync(UserRole.PLAYER)?.score
                                ?: 0
                )
                appDatabase.transactionDao().insert(quizTransaction)
            }
        }
                .doOnComplete { Timber.d("DEFAULT transaction after entering APP:%s", appDatabase.transactionDao().getAllList()) }
                .doOnError { Timber.e(it, "On Error init SCORE") }
                .onErrorComplete()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = { Timber.d("Success sync score") },
                        onError = {
                            Timber.e(it)
                        }
                )
    }
}