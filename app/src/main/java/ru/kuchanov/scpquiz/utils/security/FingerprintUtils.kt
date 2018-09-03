package ru.kuchanov.scpquiz.utils.security

import android.app.KeyguardManager
import android.content.Context.KEYGUARD_SERVICE
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
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

    private val fingerprintManagerCompat = FingerprintManagerCompat.from(App.INSTANCE)

    @RequiresApi(Build.VERSION_CODES.M)
    private val fingerprintManager = App.INSTANCE.getSystemService(FingerprintManager::class.java) as FingerprintManager

    private val keyguardManager = App.INSTANCE.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

    fun isFingerprintSupported() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        fingerprintManager.isHardwareDetected
    else
        fingerprintManagerCompat.isHardwareDetected

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSensorState() = if (isFingerprintSupported()) {
        if (!keyguardManager.isKeyguardSecure) SensorState.NOT_BLOCKED
        if (!fingerprintManager.hasEnrolledFingerprints()) SensorState.NO_FINGERPRINTS else SensorState.READY
    } else SensorState.NOT_SUPPORTED


    @RequiresApi(Build.VERSION_CODES.M)
    fun useFingerprintSensor(
        cancellationSignal: CancellationSignal,
        authenticationCallback: FingerprintManager.AuthenticationCallback
    ) {
        val cryptoObject = CryptoUtils.getCryptoObject()
        Timber.d("useFingerprintSensor cryptoObject: $cryptoObject")
        fingerprintManager.authenticate(
            cryptoObject,
            cancellationSignal,
            0,
            authenticationCallback,
            null
        )
    }
}