package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.util.SettingsView
import ru.kuchanov.scpquiz.utils.IntentUtils
import javax.inject.Inject

@InjectViewState
class SettingsPresenter @Inject constructor(
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
    }

    fun onLangClicked() = viewState.showLangsChooser(preferences.getLangs(), preferences.getLang())

    fun onSoundEnabled(checked: Boolean) = preferences.setSoundEnabled(checked)

    fun onVibrationEnabled(checked: Boolean) = preferences.setVibrationEnabled(checked)

    fun onShareClicked() = IntentUtils.tryShareApp(appContext)

    fun onPrivacyPolicyClicked() = IntentUtils.openUrl(appContext, Constants.PRIVACY_POLICY_URL)
    fun onLangSelected(selectedLang: String) {
        preferences.setLang(selectedLang)
        viewState.showLang(preferences.getLang())
    }

    fun onCoinsClicked() = router.navigateTo(Constants.Screens.MONETIZATION)
}