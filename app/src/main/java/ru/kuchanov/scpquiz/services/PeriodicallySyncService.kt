package ru.kuchanov.scpquiz.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.rxkotlin.subscribeBy
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.ui.activity.MainActivity
import ru.kuchanov.scpquiz.utils.createNotificationChannel
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

class PeriodicallySyncService : Service() {

    @Inject
    lateinit var transactionInteractor: TransactionInteractor

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, getChanelId())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Scp quiz")
                .setContentText("Sync progress")
                .setContentIntent(pendingIntent)
                .build()

        startForeground(PeriodicallySyncService.NOTIFICATION_ID, notification)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        transactionInteractor.syncAllProgress()
                .doOnComplete { Timber.d("doOnComplete before AND THEN") }
                .andThen(transactionInteractor.syncTransactions())
                .subscribeBy(
                        onComplete = {
                            Timber.d("SYnc from service complete")
                            stopServiceAndRemoveNotification()
                        },
                        onError = {
                            Timber.e(it, "SYnc from service error")
                            stopServiceAndRemoveNotification()
                        }
                )
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun stopServiceAndRemoveNotification() {
        Timber.d("stopSyncServiceAndRemoveNotification")
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
        const val NOTIFICATION_ID = 1488

        const val CHANEL_ID = "SYNC_CHANEL_ID"

        const val CHANEL_NAME = "SYNC_CHANEL_NAME"
    }
}