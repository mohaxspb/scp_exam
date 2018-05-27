package ru.kuchanov.scpquiz.mvp.presenter

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.kuchanov.scpquiz.mvp.view.EnterView
import ru.kuchanov.scpquiz.utils.StorageUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class EnterPresenter @Inject constructor(
    private var appDatabase: AppDatabase,
    private var router: Router,
    private val moshi: Moshi,
    private var quizConverter: QuizConverter,
    private val appContext: Application
) : MvpPresenter<EnterView>() {

    init {
        Timber.d("constructor")
    }

    private var dbFilled: Boolean = false
    private var secondsPast: Long = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val timerObservable = Flowable.intervalRange(0, 10, 0, 1050, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        val dbFillObservable = Single.fromCallable {
            Timber.d("read initial data from json")
            val json = StorageUtils.readFromAssets(appContext, "baseData.json")
            val type = Types.newParameterizedType(List::class.java, NwQuiz::class.java)
            val adapter = moshi.adapter<List<NwQuiz>>(type)
            adapter.fromJson(json)
        }
                .map {
                    Timber.d("write initial data to DB")
                    appDatabase.quizDao().insertQuizesWithQuizTranslations(
                        quizConverter.convertCollection(
                            it,
                            quizConverter::convert
                        ))
                    -1L
                }

        val dbFillIfEmptyObservable = Single.fromCallable { appDatabase.quizDao().getCount() }
                .flatMap {
                    if (it != 0L) {
                        Timber.d("data in DB already exists")
                        Single.just(-1L)
                    } else {
                        Timber.d("fill DB with initial data")
                        dbFillObservable
                    }
                }

        Flowable.merge(timerObservable, dbFillIfEmptyObservable.toFlowable())
                .doOnNext {
                    if (it != -1L) {
                        secondsPast = it
                    }
                }
                .flatMap {
                    if (dbFilled && secondsPast > 2) Flowable.error(IllegalStateException()) else Flowable.just(it)
                }
                .onErrorResumeNext { _: Throwable -> Flowable.empty() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Timber.d("onNext: $it")
                        if (it == -1L) {
                            dbFilled = true
                        } else {
                            viewState.showProgressText()
                            viewState.showProgressAnimation()
                        }
                    },
                    onComplete = {
                        Timber.d("onComplete")
                        router.newRootScreen(Constants.Screens.QUIZ_LIST)
                    },
                    onError = Timber::e
                )
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}