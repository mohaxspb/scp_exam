package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import android.content.Intent
import com.arellomobile.mvp.InjectViewState
import com.google.android.gms.ads.MobileAds
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.mvp.AuthPresenter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.util.SettingsView
import ru.kuchanov.scpquiz.ui.fragment.util.ScpSettingsFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import ru.kuchanov.scpquiz.utils.IntentUtils
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class ScpSettingsPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<SettingsView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor), AuthPresenter<ScpSettingsFragment> {

    override fun onAuthSuccess() {
        preferences.setIntroDialogShown(true)
        viewState.showMessage(R.string.settings_success_auth)
    }

    override lateinit var authDelegate: AuthDelegate<ScpSettingsFragment>

    override fun getAuthView(): SettingsView = viewState

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showLang(preferences.getLang())
        viewState.showSound(preferences.isSoundEnabled())
        viewState.showVibration(preferences.isVibrationEnabled())
    }

    fun onLangClicked() = preferences.getLangs()?.let { viewState.showLangsChooser(it, preferences.getLang()) }
            ?: viewState.showMessage(R.string.error_unexpected)

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

    fun onShareClicked() = IntentUtils.tryShareApp(appContext)

    fun onLogoutClicked() {
        preferences.setIntroDialogShown(false)
        preferences.setTrueAccessToken(null)
        preferences.setRefreshToken(null)
        preferences.setUserPassword(null)
        preferences.setNeverShowAuth(false)
        compositeDisposable.add(
                appDatabase.userDao().getOneByRole(UserRole.PLAYER)
                        .map { user ->
                            user.score = 0
                            appDatabase.userDao().update(user)
//                            Timber.d("USER : %s", user)
                        }
                        .map { appDatabase.transactionDao().deleteAll() }
                        .flatMap { appDatabase.finishedLevelsDao().getAllByAsc() }
                        .map { finishedLevels ->
                            appDatabase.finishedLevelsDao().update(finishedLevels.mapIndexed { index, it ->
                                it.apply {
                                    scpNameFilled = false
                                    scpNumberFilled = false
                                    nameRedundantCharsRemoved = false
                                    numberRedundantCharsRemoved = false
                                    isLevelAvailable = index < 5
//                                    Timber.d("FINISHED LEVEL : %s", it)
                                }
                            })
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onSuccess = { router.newRootScreen(Constants.Screens.ENTER) },
                                onError = {
                                    Timber.e(it)
                                    viewState.showMessage(it.message.toString())
                                }
                        )
        )
    }

    fun onResetProgressClicked() {
        compositeDisposable.add(
                apiClient.resetProgress()
                        .flatMap { it ->
                            appDatabase.userDao().getOneByRole(UserRole.PLAYER)
                                    .map { user ->
                                        user.score = it
                                        appDatabase.userDao().update(user)
//                                        Timber.d("USER : %s", user)
                                    }
                        }
                        .flatMap {
                            appDatabase.finishedLevelsDao().getAllByAsc()
                                    .map { finishedLevels ->
                                        appDatabase.finishedLevelsDao().update(finishedLevels.mapIndexed { index, it ->
                                            it.apply {
                                                scpNameFilled = false
                                                scpNumberFilled = false
                                                nameRedundantCharsRemoved = false
                                                numberRedundantCharsRemoved = false
                                                isLevelAvailable = index < 5
//                                                Timber.d("FINISHED LEVEL : %s", it)
                                            }
                                        })
                                    }
                                    .doOnSuccess {
//                                        Timber.d("BEFORE RESET :%s", appDatabase.transactionDao().getAllList())
                                        appDatabase.transactionDao().resetProgress()
//                                        Timber.d("AFTER RESET :%s", appDatabase.transactionDao().getAllList())
                                    }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { viewState.showProgress(true) }
                        .doOnEvent { _, _ -> viewState.showProgress(false) }
                        .subscribeBy(
                                onSuccess = {
                                    viewState.showMessage(R.string.reset_progress_user_message)
                                },
                                onError = {
                                    Timber.e(it)
                                    viewState.showMessage(it.toString())
                                }
                        )
        )
    }

    fun onPrivacyPolicyClicked() = IntentUtils.openUrl(appContext, Constants.PRIVACY_POLICY_URL)

    fun onLangSelected(selectedLang: String) {
        preferences.setLang(selectedLang)
        viewState.showLang(preferences.getLang())
    }

    fun onCoinsClicked() = router.navigateTo(Constants.Screens.MONETIZATION)

    fun onNavigationIconClicked() = router.exit()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }
}