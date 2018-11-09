package ru.kuchanov.scpquiz.ui.utils

import android.annotation.TargetApi
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.dialog_fingerprint.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.utils.security.FingerprintUtils
import timber.log.Timber
import javax.crypto.Cipher

object DialogUtils {

    @TargetApi(Build.VERSION_CODES.M)
    fun showFingerprintDialog(
            context: Context,
            @StringRes title: Int = R.string.dialog_fingerprints_title,
            isCancelable: Boolean = true,
            onErrorAction: () -> Unit,
            onCipherErrorAction: () -> Unit,
            onSuccessAction: (cipher: Cipher) -> Unit
    ) {
        val cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener { Timber.d("cancellationSignal canceled!") }
        val dismissFingerprintSensor = { cancellationSignal.cancel() }

        var materialDialog: MaterialDialog? = null

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fingerprint, null, false)

        val fingerprintCallback = @TargetApi(Build.VERSION_CODES.M)
        object : FingerprintManager.AuthenticationCallback() {
            /**
             * несколько неудачных попыток считывания (5)
             *
             * после этого сенсор станет недоступным на некоторое время (30 сек)
             */
            override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                super.onAuthenticationError(errMsgId, errString)
                Timber.d("onAuthenticationError: $errMsgId, $errString")
                onErrorAction.invoke()
                materialDialog?.dismiss()
            }

            /**
             * все прошло успешно
             */
            override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                Timber.d("onAuthenticationSucceeded: $result, ${result?.cryptoObject?.cipher?.parameters}")
                if (result?.cryptoObject?.cipher == null) {
                    Timber.e("cipherForDecoding is NULL!")
                    onCipherErrorAction.invoke()
                    return
                }
                onSuccessAction.invoke(result.cryptoObject.cipher)
                materialDialog?.dismiss()
            }

            /**
             * грязные пальчики, недостаточно сильный зажим
             */
            override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                super.onAuthenticationHelp(helpMsgId, helpString)
                Timber.d("onAuthenticationHelp: $helpMsgId, $helpString")
                dialogView.sensorTextView.text = helpString?.toString() ?: context.getString(R.string.try_again)
                dialogView.sensorImageView.setImageResource(R.drawable.ic_info_outline_black_24dp)
                dialogView.sensorImageView.setColorFilter(ContextCompat.getColor(context, android.R.color.black))
            }

            /**
             * отпечаток считался, но не распознался
             */
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.d("onAuthenticationFailed")
                dialogView.sensorTextView.setText(R.string.error_fingerprint_auth_failed)
                dialogView.sensorImageView.setImageResource(R.drawable.ic_warning_black_24dp)
                dialogView.sensorImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorRed))
            }
        }

        var materialDialogBuilder = MaterialDialog.Builder(context)
                .customView(dialogView, true)
                .title(title)
                .dismissListener { dismissFingerprintSensor.invoke() }
                .canceledOnTouchOutside(isCancelable)
                .cancelable(isCancelable)
                .showListener {
                    FingerprintUtils.useFingerprintSensor(
                            cancellationSignal,
                            fingerprintCallback
                    )
                }

        if (isCancelable) {
            materialDialogBuilder = materialDialogBuilder.negativeText(android.R.string.cancel)
        }

        materialDialog = materialDialogBuilder.build()

        materialDialog.show()
    }
}