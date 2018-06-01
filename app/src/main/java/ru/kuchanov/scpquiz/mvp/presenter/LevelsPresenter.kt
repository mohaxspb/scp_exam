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
import ru.kuchanov.scpquiz.model.db.FinishedLevels
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
        //todo navigate to game screen
        router.navigateTo(Constants.Screens.QUIZ, quizId)

//        Single.fromCallable { appDatabase.finishedLevelsDao().insert(FinishedLevels(quizId = quizId, finished = true)) }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy(
//                    onSuccess = {
//                        Timber.d("updated!")
//                    }
//                )
    }

    fun updateLevels() {
        Flowable.combineLatest(
            appDatabase.quizDao().getAll(),
            appDatabase.finishedLevelsDao().getAll(),
            BiFunction { t1: List<Quiz>, t2: List<FinishedLevels> -> t1 to t2 }
        )
                .map { pair ->
                    val finishedQuizesIds = pair.second.filter { it.finished }.map { it.quizId }
                    pair.first.map {
                        LevelViewModel(it, finishedQuizesIds.contains(it.id))
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