package ru.kuchanov.scpquiz.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Base64
import com.vk.sdk.util.VKUtil
import ru.kuchanov.scpquiz.App
import timber.log.Timber
import java.security.MessageDigest

object SystemUtils {

    private const val VIBRATION_DURATION = 100L

    @Suppress("unused")
    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return bytes.toString() + " B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    fun vibrate() {
        val vibrator = App.INSTANCE.getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, 125))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATION_DURATION)
        }
    }

    private fun getCertificateFingerprints(context: Context): Array<String>? =
            VKUtil.getCertificateFingerprint(context, context.packageName)

    @Suppress("unused")
    fun printCertificateFingerprints(context: Context) {
        Timber.d("sha fingerprints")
        val fingerprints = getCertificateFingerprints(context)
        if (fingerprints != null) {
            for (sha1 in fingerprints) {
                Timber.d("sha1: %s", sha1)
            }
        } else {
            Timber.e(NullPointerException(), "fingerprints arr is null!")
        }
        try {
            @SuppressLint("PackageManagerGetSignatures")
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                        context.packageName,
                        @Suppress("DEPRECATION")
                        PackageManager.GET_SIGNATURES
                )
            }
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }
            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Timber.i("printHashKey() Hash Key: %s", hashKey)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}