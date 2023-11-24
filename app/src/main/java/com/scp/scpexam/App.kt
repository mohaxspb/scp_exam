package com.scp.scpexam

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.di.Di
import com.scp.scpexam.di.module.AppModule
import com.scp.scpexam.model.db.QuizTransaction
import com.scp.scpexam.model.db.TransactionType
import com.scp.scpexam.model.db.UserRole
import com.scp.scpexam.services.DownloadWorker
import com.scp.scpexam.services.DownloadWorker.Companion.WORKER_ID
import com.scp.scpexam.utils.MyProvider
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import com.vk.api.sdk.utils.log.DefaultApiLogger
import com.vk.api.sdk.utils.log.Logger
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieApplicationModule
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@SuppressWarnings("unused")
class App : Application() {

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

        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

// initialize WorkManager
        WorkManager.initialize(this, myConfig)
        initTimber()
        initDi()
        initYandexMetrica()
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
//        VK.initialize(this)
        VK.setConfig(
            VKApiConfig(
                logger = DefaultApiLogger(
                    logLevel = lazy { Logger.LogLevel.DEBUG },
                    "VKSdkApi"
                ),
                context = this,
                appId = VK.getAppId(this),
                validationHandler = VKDefaultValidationHandler(this),
                okHttpProvider = MyProvider()
            )
        )
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        initScore()

        //use it for printing keys hash for facebook
//        Timber.d("App#onCreate")
//        SystemUtils.printCertificateFingerprints(this)
        val getArticlesRequest = PeriodicWorkRequest.Builder(
            DownloadWorker::class.java,
            30,
            TimeUnit.MINUTES
        ).build()
        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                WORKER_ID,
                ExistingPeriodicWorkPolicy.KEEP,
                getArticlesRequest
            )
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
            Toothpick.setConfiguration(toothpick.configuration.Configuration.forDevelopment())
        }
    }

    private fun initScore() {
        Completable.fromCallable {
            if (appDatabase.transactionDao()
                    .getOneByTypeNoReactive(TransactionType.UPDATE_SYNC) == null
            ) {
                val quizTransaction = QuizTransaction(
                    quizId = null,
                    transactionType = TransactionType.UPDATE_SYNC,
                    coinsAmount = appDatabase.userDao().getOneByRoleSync(UserRole.PLAYER)?.score
                        ?: 0
                )
                appDatabase.transactionDao().insert(quizTransaction)
            }
        }
            .doOnComplete {
                Timber.d(
                    "DEFAULT transaction after entering APP:%s",
                    appDatabase.transactionDao().getAllList()
                )
            }
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