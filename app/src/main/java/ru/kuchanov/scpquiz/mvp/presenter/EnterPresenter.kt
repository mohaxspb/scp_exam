package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.db.AppDatabase
import ru.kuchanov.scpquiz.mvp.view.EnterView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class EnterPresenter @Inject constructor(
    var appDatabase: AppDatabase,
    var router: Router
) : MvpPresenter<EnterView>() {

    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

//        appDatabase.quizDao().getAll().subscribeOn(Schedulers.io()).subscribeBy(
//            onNext = { Timber.d("it: $it") }
//        )

        Flowable.intervalRange(0, 3, 0, 1100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { viewState.showProgressAnimation() },
                    onComplete = { /*todo navigate to main*/ }
                )
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}