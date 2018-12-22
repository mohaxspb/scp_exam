package ru.kuchanov.scpquiz.services

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.kuchanov.scpquiz.ui.activity.MainActivity
import ru.kuchanov.scpquiz.utils.createNotificationChannel
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject


class UploadService : Service() {

    @Inject
    lateinit var appContext: Application
    @Inject
    lateinit var preferences: MyPreferenceManager
    @Inject
    lateinit var router: ScpRouter
    @Inject
    lateinit var appDatabase: AppDatabase
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var quizConverter: QuizConverter

    lateinit var notificationManager: NotificationManager

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
                .setContentTitle("Scp quiz")
                .setContentText("Downloading data")
                .setContentIntent(pendingIntent)
                .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service started")
        Maybe.fromCallable { if (appDatabase.quizDao().getCount() == 0L) null else true }
                .flatMap { apiClient.getNwQuizList().toMaybe() }
                .map { quizes -> quizes.filter { it.approved } }
                .map { quizes -> quizes.sortedBy { it.id } }.map { quizes ->
                    appDatabase.quizDao().insertQuizesWithQuizTranslations(
                            quizConverter.convertCollection(
                                    quizes,
                                    quizConverter::convert
                            )
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribeBy(
                        onError = { error: Throwable ->
                            Timber.e(error)
                            stopServiceAndRemoveNotification()
                        },
                        onComplete = {
                            Timber.d("onComplete SERVICE")
                            stopServiceAndRemoveNotification()
                        }
                )
        return Service.START_NOT_STICKY
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