package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import android.content.Intent
import com.google.android.gms.ads.MobileAds
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.controller.repository.SettingsRepository
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.db.generateRandomName
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
        override var transactionInteractor: TransactionInteractor,
        private val settingsRepository: SettingsRepository
) : BasePresenter<SettingsView>(
        appContext,
        preferences,
        router,
        appDatabase,
        apiClient,
        transactionInteractor
), AuthPresenter<ScpSettingsFragment> {

    override fun onAuthSuccess() {
        preferences.setIntroDialogShown(true)
        viewState.showMessage(R.string.settings_success_auth)
        router.exit()
    }

    override fun onAuthCanceled() {
        viewState.showMessage(R.string.canceled_auth)
    }

    override fun onAuthError() {
        viewState.showMessage(appContext.getString(R.string.auth_retry))
    }

    override lateinit var authDelegate: AuthDelegate<ScpSettingsFragment>

    override fun getAuthView(): SettingsView = viewState

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        settingsRepository.observeLanguage()
                .subscribeBy { viewState.showLang(it) }
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
        appDatabase.userDao().getOneByRole(UserRole.PLAYER)
                .map { user ->
                    user.score = 0
                    user.avatarUrl = null
                    user.name = generateRandomName(appContext)
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
                        onSuccess = { router.newRootScreen(Constants.Screens.EnterScreen) },
                        onError = {
                            Timber.e(it)
                            viewState.showMessage(it.message.toString())
                        }
                ).addTo(compositeDisposable)
    }

    fun onResetProgressClicked() {
        val resetProgressSingle = if (preferences.getTrueAccessToken() == null) {
            Single.just(0)
        } else {
            apiClient.resetProgress()
        }
        resetProgressSingle
                .flatMapCompletable { score ->
                    appDatabase
                            .userDao()
                            .getOneByRole(UserRole.PLAYER)
                            .doOnSuccess { user ->
                                user.score = score
                                appDatabase.userDao().update(user)
                            }
                            .ignoreElement()
                }
                .andThen(
                        appDatabase
                                .finishedLevelsDao()
                                .getAllByAsc()
                                .map { finishedLevels ->
                                    appDatabase
                                            .finishedLevelsDao()
                                            .update(finishedLevels.mapIndexed { index, finishedLevel ->
                                                finishedLevel.apply {
                                                    scpNameFilled = false
                                                    scpNumberFilled = false
                                                    nameRedundantCharsRemoved = false
                                                    numberRedundantCharsRemoved = false
                                                    isLevelAvailable = index < 5
                                                }
                                            })
                                }
                                .doOnSuccess { appDatabase.transactionDao().resetProgress() }
                )
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
                ).addTo(compositeDisposable)
    }

    fun onPrivacyPolicyClicked() = IntentUtils.openUrl(appContext, Constants.PRIVACY_POLICY_URL)

    fun onLangSelected(selectedLang: String) {
        settingsRepository.setLanguage(selectedLang)
        viewState.showMessage(R.string.toast_text_changed_language)
        router.newRootScreen(Constants.Screens.LevelsScreen)
    }

    fun onCoinsClicked() = router.navigateTo(Constants.Screens.MonetizationScreen)

    fun onNavigationIconClicked() = router.exit()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }
}
