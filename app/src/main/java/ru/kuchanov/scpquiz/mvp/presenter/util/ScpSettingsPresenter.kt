package ru.kuchanov.scpquiz.mvp.presenter.util

import android.annotation.TargetApi
import android.app.Application
import android.os.Build
import com.arellomobile.mvp.InjectViewState
import com.google.android.gms.ads.MobileAds
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.util.SettingsView
import ru.kuchanov.scpquiz.utils.IntentUtils
import ru.kuchanov.scpquiz.utils.security.CryptoUtils
import ru.kuchanov.scpquiz.utils.security.FingerprintUtils
import ru.kuchanov.scpquiz.utils.security.SensorState
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject

@InjectViewState
class ScpSettingsPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    override var appDatabase: AppDatabase
) : BasePresenter<SettingsView>(appContext, preferences, router, appDatabase) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.showLang(preferences.getLang())
        viewState.showSound(preferences.isSoundEnabled())
        viewState.showVibration(preferences.isVibrationEnabled())
        viewState.showFingerprint(preferences.isFingerprintEnabled())
    }

    fun onLangClicked() = viewState.showLangsChooser(preferences.getLangs(), preferences.getLang())

    fun onSoundEnabled(enabled: Boolean) {
        preferences.setSoundEnabled(enabled)
        if (enabled) {
            MobileAds.setAppMuted(false)
            MobileAds.setAppVolume(0.5f)
        } else {
            // Set app volume to be half of current device volume.
            MobileAds.setAppMuted(true)
        }
    }

    fun onVibrationEnabled(checked: Boolean) = preferences.setVibrationEnabled(checked)

    @TargetApi(Build.VERSION_CODES.M)
    fun onFingerPrintEnabled(checked: Boolean) {
        Timber.d("onFingerPrintEnabled: $checked")

        when (FingerprintUtils.getSensorState()) {
            SensorState.READY -> {
                Timber.d("READY")
                viewState.showFingerprintDialog(true)
            }
            SensorState.NO_FINGERPRINTS -> {
                Timber.d("NO_FINGERPRINTS")
                preferences.setFingerprintEnabled(false)
                viewState.showFingerprint(false)
                viewState.showMessage(R.string.error_no_fingerprints)
            }
            SensorState.NOT_BLOCKED -> {
                Timber.d("NOT_BLOCKED")
                preferences.setFingerprintEnabled(false)
                viewState.showFingerprint(false)
            }
            SensorState.NOT_SUPPORTED -> {
                Timber.d("NOT_SUPPORTED")
                preferences.setFingerprintEnabled(false)
                viewState.showFingerprint(false)
            }
        }
    }

    fun onShareClicked() = IntentUtils.tryShareApp(appContext)

    fun onPrivacyPolicyClicked() = IntentUtils.openUrl(appContext, Constants.PRIVACY_POLICY_URL)

    fun onLangSelected(selectedLang: String) {
        preferences.setLang(selectedLang)
        viewState.showLang(preferences.getLang())
    }

    fun onCoinsClicked() = router.navigateTo(Constants.Screens.MONETIZATION)

    fun onNavigationIconClicked() = router.exit()

    @TargetApi(Build.VERSION_CODES.M)
    fun onFingerprintAuthSucceeded(cipherForDecoding: Cipher?) {
        if (cipherForDecoding == null) {
            Timber.e("cipherForDecoding is NULL!")
            viewState.showMessage(R.string.error_get_chipher)
            return
        }
        if (preferences.getUserPassword() == null) {
            //write password for user if it isn't existed yet
            //it must be 6 signs number, which we encrypt and save to preferences
            val password = CryptoUtils.generateUserPassword()
            Timber.d("passwordEncoded: $password")
            if (password != null) {
                preferences.setUserPassword(password)
            } else {
                viewState.showMessage(R.string.error_create_password)
                return
            }
            //here we have password and save it...
            //todo so we just need to say that everything is OK and fingerprint set/removed
        }
        val encodedPassword = preferences.getUserPassword()
        Timber.d("encodedPassword: $encodedPassword")
        val decodedPassword = encodedPassword?.let { CryptoUtils.decode(it, cipherForDecoding) }
        Timber.d("passwordDecoded: $decodedPassword")
        if (decodedPassword != null) {
            //OK
            //now we can use decoded data to login. I.e. send auth request to server, use token to get data, or simulate pin enter
            //in our case we'll only navigate to levels screen while open app
            //and now we simply say user result - enable/disable this feature
            //todo show message for enable/disable cases
            viewState.showMessage(R.string.fingerprint_access_enabled)
        } else {
            //some error occurred while decode password
            viewState.showMessage(R.string.error_decode_password)
        }
    }
}