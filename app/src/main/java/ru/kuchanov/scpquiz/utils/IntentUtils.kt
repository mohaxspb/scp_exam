package ru.kuchanov.scpquiz.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import android.widget.Toast
import ru.kuchanov.scpquiz.R

object IntentUtils {

    fun tryShareApp(context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_link_with_text, context.packageName))
        intent.type = "text/plain"
        context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.share_choser_text)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        checkAndStart(context, intent, R.string.error_start_market)
    }

    fun tryOpenPlayMarket(context: Context, appId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.market_url, appId)))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        checkAndStart(context, intent, R.string.error_start_market)
    }

    fun checkIntent(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)
        return activities.size > 0
    }

    private fun checkAndStart(context: Context, intent: Intent, @StringRes errorRes: Int) {
        if (checkIntent(context, intent)) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, context.getString(errorRes), Toast.LENGTH_SHORT).show()
        }
    }
}