package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.db.AppDatabase
import ru.kuchanov.scpquiz.mvp.view.EnterView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class EnterPresenter @Inject constructor(
    var appDatabase: AppDatabase
    ,
    var router: Router
) : MvpPresenter<EnterView>() {

//    @Inject
//    lateinit var appDatabase: AppDatabase
//    @Inject
//    lateinit var router: Router
//
    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        appDatabase.quizDao().getAll().subscribeOn(Schedulers.io()).subscribeBy(
            onNext = { Timber.d("it: $it") }
        )
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}