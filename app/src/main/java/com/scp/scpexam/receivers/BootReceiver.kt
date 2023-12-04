package com.scp.scpexam.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.scp.scpexam.services.DownloadWorker
import com.scp.scpexam.services.PeriodicallySyncWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Timber.d("onReceive with action: %s", intent?.action)
        if (intent?.action == (Intent.ACTION_BOOT_COMPLETED) ||
            intent?.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val getArticlesRequest = PeriodicWorkRequest.Builder(
                DownloadWorker::class.java,
                30,
                TimeUnit.MINUTES
            ).build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    DownloadWorker.PERIODIC_WORKER_ID,
                    ExistingPeriodicWorkPolicy.KEEP,
                    getArticlesRequest
                )
            val syncRequest = PeriodicWorkRequest.Builder(
                PeriodicallySyncWorker::class.java,
                30,
                TimeUnit.MINUTES
            ).build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    PeriodicallySyncWorker.PERIODICALLY_SYNC_PERIODIC_WORKER_ID,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )

        }
    }
}