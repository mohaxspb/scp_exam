package com.scp.scpexam.services

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import com.scp.scpexam.R
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.LevelsInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import com.scp.scpexam.model.api.QuizConverter
import com.scp.scpexam.model.util.QuizFilter
import com.scp.scpexam.ui.activity.MainActivity
import com.scp.scpexam.utils.createNotificationChannel
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject


class DownloadService : Service() {

    @Inject
    lateinit var appContext: Application
    @Inject
    lateinit var preferences: MyPreferenceManager
    @Inject
    lateinit var router: Router
    @Inject
    lateinit var appDatabase: AppDatabase
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var quizConverter: QuizConverter
    @Inject
    lateinit var quizFilter: QuizFilter
    @Inject
    lateinit var levelsInteractor: LevelsInteractor

    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, getChanelId())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.download_notification))
                .setContentIntent(pendingIntent)
                .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service started")
        // Maybe нужен на случай ответ с сервера пришёл раньше чем зпсались данные с asset
        Maybe
                .fromCallable {
                    if (appDatabase.quizDao().getCount() == 0L) {
                        //null indicates `NO VALUE IN REACTIVE SOURCE`
                        //so chain will complete with `onComplete` event.
                        null
                    } else {
                        //some value will cause chain to process
                        //so it will complete with onError or onSuccess event.
                        //onComplete event will never fired
                        true
                    }
                }
//                .flatMap { apiClient.getNwQuizList().toMaybe() }
                .flatMap { levelsInteractor.downloadQuizzesPaging().toMaybe() }
                .map {
//                                        Timber.d("apiClient.getNwQuizList().toMaybe() :%s", it)
                    quizFilter.filterQuizzes(it)
                }
                .map { quizzes ->
                    //                    Timber.d("quizFilter.filterQuizzes(it) :%s", quizzes)
                    quizzes.sortedBy { it.id }
                }
                .map { sortedQuizList ->
                    //                    Timber.d("sortedQuizList :%s", sortedQuizList)
                    val quizzesFromBd = appDatabase.quizDao().getAllList()
//                    Timber.d("quizzesFromDb :%s", quizzesFromBd)
                    quizzesFromBd.forEach { quizFromDb ->
                        //                        Timber.d("quizFromDb :%s", quizFromDb)
                        val quizFromDbInListFromServer = sortedQuizList.find {
                            //                            Timber.d("NwQuizInFind :%s", it)
//                            Timber.d("quizFromDbInFind :%s", quizFromDb)
                            it.id == quizFromDb.id
                        }
//                        Timber.d("quizFromDbInListFromServer :%s", quizFromDbInListFromServer)
                        if (quizFromDbInListFromServer == null) {
                            appDatabase.transactionDao().deleteAllTransactionsByQuizId(quizFromDb.id)
                            appDatabase.finishedLevelsDao().deleteAllFinishedLevelsByQuizId(quizFromDb.id)
                            appDatabase.quizDao().delete(quizFromDb)
                        }
                    }
                    return@map sortedQuizList
                }
                .doOnSuccess { quizzes ->
                    appDatabase
                            .quizDao()
                            .insertQuizesWithQuizTranslationsWithFinishedLevels(
                                    quizConverter.convertCollection(
                                            quizzes,
                                            quizConverter::convert
                                    )
                            )
                    val langs = appDatabase.quizTranslationsDao().getAllLangs().toSet()
                    preferences.setLangs(langs)

                }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .doFinally { stopServiceAndRemoveNotification() }
                .subscribeBy(
                        onSuccess = { Timber.d("onSuccess service: ${it.size} quizzes inserted") },
                        onError = { Timber.e(it, "onError service while update quizzes from server") },
                        onComplete = { Timber.d("onComplete service") }
                )
        return START_NOT_STICKY
    }

    private fun stopServiceAndRemoveNotification() {
        Timber.d("stopServiceAndRemoveNotification")
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
    }

    private fun getChanelId(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                    this,
                    CHANEL_ID,
                    CHANEL_NAME
            )
        } else {
            ""
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1337

        const val CHANEL_ID = "DOWNLOADS_CHANEL_ID"

        const val CHANEL_NAME = "DOWNLOADS_CHANEL_NAME"
    }
}
