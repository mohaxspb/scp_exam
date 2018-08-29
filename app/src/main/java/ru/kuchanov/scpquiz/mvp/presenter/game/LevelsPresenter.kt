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
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.ui.QuizScreenLaunchData
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.game.LevelsView
import ru.kuchanov.scpquiz.utils.BitmapUtils
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class LevelsPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    override var appDatabase: AppDatabase
) : BasePresenter<LevelsView>(appContext, preferences, router, appDatabase) {

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
                            quiz,
                            finishedLevel.scpNameFilled,
                            finishedLevel.scpNumberFilled
                        )
                    }

                    return@map Pair(levels, triple.third)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Timber.d("loadLevels onNext")
                        viewState.showLevels(it.first)
                        viewState.showCoins(it.second.score)
                    }
                )
    }
}