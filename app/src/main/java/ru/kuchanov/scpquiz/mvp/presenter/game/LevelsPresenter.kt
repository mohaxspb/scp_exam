package ru.kuchanov.scpquiz.mvp.presenter.game

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.ui.QuizScreenLaunchData
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.game.LevelsView
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@InjectViewState
class LevelsPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    override var appDatabase: AppDatabase
) : BasePresenter<LevelsView>(appContext, preferences, router, appDatabase) {

    private lateinit var levels: List<Quiz>

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        updateLevels()
    }

    fun onLevelClick(levelViewModel: LevelViewModel) {
        Timber.d("onLevelClick: %s", levelViewModel.quiz.id)
        val showAds = !preferences.isAdsDisabled()
                && preferences.isNeedToShowInterstitial()
                && (!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled)
        Timber.d(
            "!preferences.isAdsDisabled()\n" +
                    "preferences.isNeedToShowInterstitial()\n" +
                    "(!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled): %s/%s/%s",
            !preferences.isAdsDisabled(),
            preferences.isNeedToShowInterstitial(),
            !levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled
        )
        Timber.d("showAds: $showAds")
        router.navigateTo(Constants.Screens.QUIZ, QuizScreenLaunchData(levelViewModel.quiz.id, !showAds))
    }

    private fun updateLevels() {
        Flowable.combineLatest(
            appDatabase.quizDao().getAll(),
            appDatabase.finishedLevelsDao().getAll(),
            BiFunction { quizes: List<Quiz>, finishedLevels: List<FinishedLevel> -> quizes to finishedLevels }
        )

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { pair ->
                    val isAllLevelsFinished = !pair.second.any { !it.scpNumberFilled || !it.scpNameFilled }
                    viewState.showAllLevelsFinishedPanel(isAllLevelsFinished)
                }
                .observeOn(Schedulers.io())
                .map { pair ->
                    Timber.d("pair.second: ${pair.second.size}")

                    levels = LinkedList(pair.first)

                    pair.first.map { quiz ->
                        val finishedLevel = pair.second.find { it.quizId == quiz.id }
                                ?: throw IllegalStateException("level not found for quizId: ${quiz.id}")
                        LevelViewModel(
                            quiz,
                            finishedLevel.scpNameFilled,
                            finishedLevel.scpNumberFilled
                        )
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Timber.d("updateLevels onNext")
                        viewState.showLevels(it)
                    }
                )
    }
}