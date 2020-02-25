package com.scp.scpexam.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.scp.scpexam.Constants
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import com.scp.scpexam.services.DownloadService
import com.scp.scpexam.services.PeriodicallySyncService
import toothpick.Toothpick
import javax.inject.Inject

class AutoSyncReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferences: MyPreferenceManager

    override fun onReceive(context: Context, intent: Intent) {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))

        if (preferences.getTrueAccessToken() != null) {
            val periodicallyServiceIntent = Intent(context, PeriodicallySyncService::class.java)
            ContextCompat.startForegroundService(context, periodicallyServiceIntent)
        }

        val downloadServiceIntent = Intent(context, DownloadService::class.java)
        ContextCompat.startForegroundService(context, downloadServiceIntent)
    }

    companion object {
        fun setAlarm(context: Context) {
            cancelAlarm(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val syncIntent = Intent(context, AutoSyncReceiver::class.java)
            val pendingSyncIntent = PendingIntent.getBroadcast(context, 0, syncIntent, 0)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.SYNC_PERIOD, pendingSyncIntent)
        }

        private fun cancelAlarm(context: Context) {
            val intent = Intent(context, AutoSyncReceiver::class.java)
            val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sender)
        }
    }
}