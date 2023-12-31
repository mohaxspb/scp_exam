package com.scp.scpexam.mvp.presenter.game

import android.app.Application
import android.graphics.Bitmap
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.viewmodel.LevelViewModel
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.LevelsInteractor
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.controller.repository.SettingsRepository
import com.scp.scpexam.model.db.*
import com.scp.scpexam.model.ui.QuizScreenLaunchData
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.game.LevelsView
import com.scp.scpexam.utils.BitmapUtils
import com.scp.scpexam.utils.addTo
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@InjectViewState
class LevelsPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: Router,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor,
        val settingsRepository: SettingsRepository,
        private val levelsInteractor: LevelsInteractor
) : BasePresenter<LevelsView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    lateinit var player: User

    private val quizProgressStates = TreeMap<Long, LevelViewModel>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        loadLevels()
    }

    fun onLevelClick(levelViewModel: LevelViewModel) {
        val showAds = !preferences.isAdsDisabled()
                && preferences.isNeedToShowInterstitial()
                && (!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled)
//        Timber.d(
//                "!preferences.isAdsDisabled()\npreferences.isNeedToShowInterstitial()\n(!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled): %s/%s/%s",
//                !preferences.isAdsDisabled(),
//                preferences.isNeedToShowInterstitial(),
//                !levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled
//        )
//        Timber.d("showAds: $showAds")
        router.navigateTo(Constants.Screens.GameScreen(QuizScreenLaunchData(levelViewModel.quiz.id, !showAds)))
    }

    fun onCoinsClicked() = viewState.onNeedToOpenCoins()

    fun openCoins(bitmap: Bitmap) {
        Completable.fromAction {
            BitmapUtils.persistImage(
                    appContext,
                    bitmap,
                    Constants.SETTINGS_BACKGROUND_FILE_NAME)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = { router.navigateTo(Constants.Screens.MonetizationScreen) }
                )
                .addTo(compositeDisposable)
    }

    fun onHamburgerMenuClicked() = viewState.onNeedToOpenSettings()

    fun openSettings(bitmap: Bitmap) {
        Completable.fromAction {
            BitmapUtils.persistImage(
                    appContext,
                    bitmap,
                    Constants.SETTINGS_BACKGROUND_FILE_NAME)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = { router.navigateTo(Constants.Screens.SettingsScreen) }
                )
                .addTo(compositeDisposable)
    }

    private fun loadLevels() {
        Flowable
                .combineLatest(
                        settingsRepository.observeLanguage()
                                .doOnNext { quizProgressStates.clear() }
                                .flatMap { appDatabase.quizDao().getAllForLang(it) },
                        appDatabase.finishedLevelsDao().getAll(),
                        appDatabase.userDao().getByRoleWithUpdates(UserRole.PLAYER).map { it.first() },
                        Function3 { quizes: List<Quiz>, finishedLevels: List<FinishedLevel>, player: User ->
                            Triple(
                                    quizes,
                                    finishedLevels,
                                    player
                            )
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { pair ->
                    val isAllLevelsFinished = !pair.second.any { !it.scpNumberFilled || !it.scpNameFilled }
                    viewState.showAllLevelsFinishedPanel(isAllLevelsFinished)
                }
                .observeOn(Schedulers.io())
                .map { triple ->
                    triple.first.forEach { quiz ->
                        val finishedLevel = triple.second.find { it.quizId == quiz.id }
                                ?: return@forEach
                        quizProgressStates.getOrPut(quiz.id) {
                            LevelViewModel(
                                    quiz = quiz,
                                    scpNameFilled = finishedLevel.scpNameFilled,
                                    scpNumberFilled = finishedLevel.scpNumberFilled,
                                    isLevelAvailable = finishedLevel.isLevelAvailable,
                                    showProgress = false
                            )
                        }
                                .apply {
                                    this.quiz = quiz
                                    scpNameFilled = finishedLevel.scpNameFilled
                                    scpNumberFilled = finishedLevel.scpNumberFilled
                                    isLevelAvailable = finishedLevel.isLevelAvailable
                                }
                    }
                    return@map Pair(quizProgressStates.values.toList(), triple.third)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.d("loadLevels onNext: ${it.first.size}")
                            viewState.showLevels(it.first)
                            player = it.second
                            viewState.showCoins(it.second.score)
                        },
                        onError = {
                            Timber.e(it, "error while load levels")
                        }
                )
                .addTo(compositeDisposable)
    }

    fun onLevelsClick() {
        if (BuildConfig.DEBUG) {
            appDatabase.userDao()
                    .getOneByRole(UserRole.PLAYER)
                    .map {
                        it.score = 0
                        appDatabase.userDao().update(it)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
    }

    fun onLevelUnlockClicked(levelViewModel: LevelViewModel, itemPosition: Int) {
        if (player.score >= Constants.COINS_FOR_LEVEL_UNLOCK) {
            compositeDisposable.add(appDatabase.finishedLevelsDao()
                    .getByIdOrErrorOnce(levelViewModel.quiz.id)
                    .map {
                        it.isLevelAvailable = true
                        appDatabase.finishedLevelsDao().update(it)
                    }
                    .flatMap { appDatabase.userDao().getOneByRole(UserRole.PLAYER) }
                    .map {
                        it.score -= Constants.COINS_FOR_LEVEL_UNLOCK
                        appDatabase.userDao().update(it)
                    }

                    .flatMapCompletable { transactionInteractor.makeTransaction(levelViewModel.quiz.id, TransactionType.LEVEL_ENABLE_FOR_COINS, -Constants.COINS_FOR_LEVEL_UNLOCK) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        levelViewModel.showProgress = true
                        viewState.showProgressOnQuizLevel(itemPosition)
                    }
                    .doOnEvent {
                        levelViewModel.showProgress = false
                        viewState.showProgressOnQuizLevel(itemPosition)
                    }
                    .subscribeBy(
                            onError = {
                                Timber.e(it)
                                viewState.showMessage(it.message ?: "Unexpected error")
                            }
                    ))
        } else {
            viewState.showMessage(R.string.message_not_enough_coins_level_unlock)
        }
    }

    fun getAllQuizzes(){
        levelsInteractor.downloadQuizzesPaging()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewState.showProgress(true)
                    viewState.showSwipeProgressBar(false)
                }
                .doOnEvent { _, _ ->
                    viewState.showProgress(false)
                    viewState.showSwipeProgressBar(false)
                }
                .subscribeBy (
                        onError = {
                            Timber.e(it)
                            viewState.showMessage(it.message ?: "Unexpected error")
                        }
                )
                .addTo(compositeDisposable)
    }

    fun onLeaderboardButtonClicked() {
        router.navigateTo(Constants.Screens.LeaderboardScreen)
    }
}
