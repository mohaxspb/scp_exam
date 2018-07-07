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
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.Quiz
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

    fun onLevelClick(quizId: Long) {
        Timber.d("onLevelClick: %s", quizId)
        router.navigateTo(Constants.Screens.QUIZ, quizId)
    }

    private fun updateLevels() {
        Flowable.combineLatest(
            appDatabase.quizDao().getAll(),
            appDatabase.finishedLevelsDao().getAll(),
            BiFunction { t1: List<Quiz>, t2: List<FinishedLevel> -> t1 to t2 }
        )
                .map { pair ->
                    Timber.d("pair.second: ${pair.second}")

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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Timber.d("updateLevels onNext")
                        viewState.showLevels(it)
                    }
                )
    }
}