package ru.kuchanov.scpquiz.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.services.DownloadService
import ru.kuchanov.scpquiz.services.PeriodicallySyncService


class AutoSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val periodicallyServiceIntent = Intent(context, PeriodicallySyncService::class.java)
        ContextCompat.startForegroundService(context, periodicallyServiceIntent)

        val downloadServiceIntent = Intent(context, DownloadService::class.java)
        ContextCompat.startForegroundService(context, downloadServiceIntent)
    }

    companion object {
        fun setAlarm(context: Context) {
            cancelAlarm(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val syncIntent = Intent(context, AutoSyncReceiver::class.java)
            val pendingSyncIntent = PendingIntent.getBroadcast(context, 0, syncIntent, 0)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.SYNC_PERIOD, pendingSyncIntent) // Millisec * Second * Minute
        }

        private fun cancelAlarm(context: Context) {
            val intent = Intent(context, AutoSyncReceiver::class.java)
            val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sender)
        }
    }
}


