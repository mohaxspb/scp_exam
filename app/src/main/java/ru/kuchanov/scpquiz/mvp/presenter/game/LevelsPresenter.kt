package ru.kuchanov.scpquiz.mvp.presenter.game

import android.app.Application
import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.*
import ru.kuchanov.scpquiz.model.ui.QuizScreenLaunchData
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.game.LevelsView
import ru.kuchanov.scpquiz.utils.BitmapUtils
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@InjectViewState
class LevelsPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<LevelsView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    lateinit var player: User

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        loadLevels()
    }

    fun onLevelClick(levelViewModel: LevelViewModel) {
        Timber.d("onLevelClick: %s", levelViewModel.quiz.id)
        val showAds = !preferences.isAdsDisabled()
                && preferences.isNeedToShowInterstitial()
                && (!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled)
        Timber.d(
                "!preferences.isAdsDisabled()\npreferences.isNeedToShowInterstitial()\n(!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled): %s/%s/%s",
                !preferences.isAdsDisabled(),
                preferences.isNeedToShowInterstitial(),
                !levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled
        )
        Timber.d("showAds: $showAds")
        router.navigateTo(Constants.Screens.QUIZ, QuizScreenLaunchData(levelViewModel.quiz.id, !showAds))
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
                        onComplete = { router.navigateTo(Constants.Screens.MONETIZATION) }
                )
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
                        onComplete = { router.navigateTo(Constants.Screens.SETTINGS) }
                )
    }

    private fun loadLevels() {
        Flowable.combineLatest(
                appDatabase.quizDao().getAll(),
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
                    val levels = triple.first.map { quiz ->
                        val finishedLevel = triple.second.find { it.quizId == quiz.id }
                                ?: throw IllegalStateException("level not found for quizId: ${quiz.id}")
                        LevelViewModel(
                                quiz = quiz,
                                scpNameFilled = finishedLevel.scpNameFilled,
                                scpNumberFilled = finishedLevel.scpNumberFilled,
                                isLevelAvailable = finishedLevel.isLevelAvailable
                        )
                    }

                    return@map Pair(levels, triple.third)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.d("loadLevels onNext")
                            viewState.showLevels(it.first)
                            player = it.second
                            viewState.showCoins(it.second.score)
                        },
                        onError = {
                            Timber.e(it, "error while load levels")
                        }
                )
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
        levelViewModel.showProgress = true
        viewState.showProgressOnQuizLevel(itemPosition)
        Timber.d("OnLevelUnlockClicked")
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
                    .delay(5000, TimeUnit.MILLISECONDS)
                    .flatMapCompletable { transactionInteractor.makeTransaction(levelViewModel.quiz.id, TransactionType.LEVEL_ENABLE_FOR_COINS, -Constants.COINS_FOR_LEVEL_UNLOCK) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = {
                                Timber.d("OnLevelUnlockClicked ON ERROR")
                                levelViewModel.showProgress = false
                                viewState.showProgressOnQuizLevel(itemPosition)
                                Timber.e(it)
                                viewState.showMessage(it.message ?: "Unexpected error")
                            },
                            onComplete = {
                                Timber.d("OnLevelUnlockClicked ONCOMPLETE")
                                levelViewModel.showProgress = false
                                viewState.showProgressOnQuizLevel(itemPosition)
                                Timber.d("Success transaction from Level Presenter")
                            }
                    ))
        } else {
            Timber.d("OnLevelUnlockClicked ELSE NO COINS")
            levelViewModel.showProgress = false
            viewState.showProgressOnQuizLevel(itemPosition)
            viewState.showMessage(R.string.message_not_enough_coins_level_unlock)
        }
    }
}
