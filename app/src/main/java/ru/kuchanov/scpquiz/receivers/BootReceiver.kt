package ru.kuchanov.scpquiz.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.kuchanov.scpquiz.receivers.AutoSyncReceiver.Companion.setAlarm
import timber.log.Timber


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("onReceive with action: %s", intent?.action)
        if (intent?.action == (Intent.ACTION_BOOT_COMPLETED) ||
                intent?.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
                intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            context?.let { setAlarm(it) }
        }
    }
}