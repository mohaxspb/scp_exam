package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.mvp.view.LevelsView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class LevelsPresenter @Inject constructor(
    private var appDatabase: AppDatabase,
    private var router: Router
) : MvpPresenter<LevelsView>() {

    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        updateLevels()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
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
                    pair.first.map { quiz ->
                        val finishedLevel = pair.second.find { it.quizId == quiz.id }
                        LevelViewModel(
                            quiz,
                            finishedLevel?.scpNameFilled ?: false,
                            finishedLevel?.scpNumberFilled ?: false
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Timber.d("updateLevels onNext")
//                        Timber.d("updateLevels: $it")
                        viewState.showLevels(it)
                    }
                )
    }
}