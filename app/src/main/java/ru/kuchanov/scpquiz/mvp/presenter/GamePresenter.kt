package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.db.FinishedLevels
import ru.kuchanov.scpquiz.mvp.view.GameView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class GamePresenter @Inject constructor(
    private var appDatabase: AppDatabase,
    private var router: Router
) : MvpPresenter<GameView>() {

    var quizId: Long by Delegates.notNull()

    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        loadLevel()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    fun loadLevel() {
        //todo load level and some other levels to create keyboard
    }

    fun onLevelCompleted() {
        //mark level as completed
        Single.fromCallable { appDatabase.finishedLevelsDao().insert(FinishedLevels(quizId = quizId, finished = true)) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        Timber.d("updated!")
                    }
                )
    }
}