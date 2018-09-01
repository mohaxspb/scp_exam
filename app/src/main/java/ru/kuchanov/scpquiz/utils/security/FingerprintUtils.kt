package ru.kuchanov.scpquiz.utils.security

import android.app.KeyguardManager
import android.content.Context.KEYGUARD_SERVICE
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import ru.kuchanov.scpquiz.App
import timber.log.Timber

enum class SensorState {
    NOT_SUPPORTED,
    NOT_BLOCKED, // если устройство не защищено пином, рисунком или паролем
    NO_FINGERPRINTS, // если на устройстве нет отпечатков
    READY
}

object FingerprintUtils {

    private val fingerprintManager = FingerprintManagerCompat.from(App.INSTANCE)

    private val keyguardManager = App.INSTANCE.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

    fun isFingerprintSupported() = fingerprintManager.isHardwareDetected

    fun getSensorState() = if (isFingerprintSupported()) {
        if (!keyguardManager.isKeyguardSecure) SensorState.NOT_BLOCKED
        if (!fingerprintManager.hasEnrolledFingerprints()) SensorState.NO_FINGERPRINTS else SensorState.READY
    } else SensorState.NOT_SUPPORTED


    @RequiresApi(Build.VERSION_CODES.M)
    fun useFingerprintSensor() {
        fingerprintManager.authenticate(
            CryptoUtils.getCryptoObject(),
            0,
            CancellationSignal(),
            object : FingerprintManagerCompat.AuthenticationCallback() {
                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errMsgId, errString)
                    Timber.d("onAuthenticationError: $errMsgId, $errString")
                }

                override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    Timber.d("onAuthenticationSucceeded: $result, ${result?.cryptoObject?.cipher}")
                }

                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                    super.onAuthenticationHelp(helpMsgId, helpString)
                    Timber.d("onAuthenticationHelp: $helpMsgId, $helpString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.d("onAuthenticationFailed")
                }
            },
            null
        )
    }
}