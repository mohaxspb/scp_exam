package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.db.AppDatabase
import ru.kuchanov.scpquiz.mvp.view.MainView
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
    private val appDatabase: AppDatabase
) : MvpPresenter<MainView>() {

    init {
        Timber.d("constructor")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    //    fun onSomethingClick() = viewState.showMessage("onSomethingClick")
    fun onSomethingClick() {
        Single.fromCallable { appDatabase.quizDao().getQuizWithQuizTranslations(2) }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onError = { Timber.e(it) },
                    onSuccess = { Timber.d("quiz: $it") }
                )
    }
}