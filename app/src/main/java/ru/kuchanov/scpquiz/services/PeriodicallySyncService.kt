package ru.kuchanov.scpquiz.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.sync_notification))
                .setContentIntent(pendingIntent)
                .build()

        startForeground(NOTIFICATION_ID, notification)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        transactionInteractor.syncAllProgress()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
        return START_NOT_STICKY
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