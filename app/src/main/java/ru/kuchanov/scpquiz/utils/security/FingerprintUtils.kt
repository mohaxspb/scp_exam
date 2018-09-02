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
    /**
     * in case devices is not protected with pin, graphic key or password
     */
    NOT_BLOCKED,
    /**
     * If there is no fingerprints on device
     */
    NO_FINGERPRINTS,
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
    fun useFingerprintSensor(
        cancellationSignal: CancellationSignal,
        authenticationCallback: FingerprintManagerCompat.AuthenticationCallback
    ) {
        val cryptoObject = CryptoUtils.getCryptoObject()
        Timber.d("useFingerprintSensor cryptoObject: $cryptoObject")
        fingerprintManager.authenticate(
            cryptoObject,
            0,
            cancellationSignal,
            authenticationCallback,
            null
        )
    }
}