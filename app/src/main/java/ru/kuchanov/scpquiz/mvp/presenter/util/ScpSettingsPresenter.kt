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
import ru.kuchanov.scpquiz.utils.security.FingerprintUtils
import ru.kuchanov.scpquiz.utils.security.SensorState
import timber.log.Timber
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
                viewState.showFingerprint(false)
                viewState.showMessage(R.string.error_no_fingerprints)
            }
            SensorState.NOT_BLOCKED -> {
                Timber.d("NOT_BLOCKED")
                viewState.showFingerprint(false)
            }
            SensorState.NOT_SUPPORTED -> {
                Timber.d("NOT_SUPPORTED")
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
}