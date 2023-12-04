package com.scp.scpexam.services

import android.app.Application
import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.LevelsInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import com.scp.scpexam.model.api.QuizConverter
import com.scp.scpexam.model.util.QuizFilter
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    companion object {
        const val PERIODIC_WORKER_ID = "PERIODIC_WORKER_ID"
        const val ONE_TIME_WORKER_ID = "ONE_TIME_WORKER_ID"
    }

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var appContext: Application

    @Inject
    lateinit var preferences: MyPreferenceManager

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var apiClient: ApiClient

    @Inject
    lateinit var quizConverter: QuizConverter

    @Inject
    lateinit var quizFilter: QuizFilter

    @Inject
    lateinit var levelsInteractor: LevelsInteractor

    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    override fun createWork(): Single<Result> {
        Timber.d("Worker started")
        // Maybe нужен на случай ответ с сервера пришёл раньше чем зпсались данные с asset
        return Single.create { emitter ->
            Maybe
                .fromCallable {
                    if (appDatabase.quizDao().getCount() == 0L) {
                        //null indicates `NO VALUE IN REACTIVE SOURCE`
                        //so chain will complete with `onComplete` event.
                        null
                    } else {
                        //some value will cause chain to process
                        //so it will complete with onError or onSuccess event.
                        //onComplete event will never fired
                        true
                    }
                }
                .flatMap { levelsInteractor.downloadQuizzesPaging().toMaybe() }
                .map {
//                                        Timber.d("apiClient.getNwQuizList().toMaybe() :%s", it)
                    quizFilter.filterQuizzes(it)
                }
                .map { quizzes ->
                    //                    Timber.d("quizFilter.filterQuizzes(it) :%s", quizzes)
                    quizzes.sortedBy { it.id }
                }
                .map { sortedQuizList ->
                    //                    Timber.d("sortedQuizList :%s", sortedQuizList)
                    val quizzesFromBd = appDatabase.quizDao().getAllList()
//                    Timber.d("quizzesFromDb :%s", quizzesFromBd)
                    quizzesFromBd.forEach { quizFromDb ->
                        //                        Timber.d("quizFromDb :%s", quizFromDb)
                        val quizFromDbInListFromServer = sortedQuizList.find {
                            //                            Timber.d("NwQuizInFind :%s", it)
//                            Timber.d("quizFromDbInFind :%s", quizFromDb)
                            it.id == quizFromDb.id
                        }
//                        Timber.d("quizFromDbInListFromServer :%s", quizFromDbInListFromServer)
                        if (quizFromDbInListFromServer == null) {
                            appDatabase.transactionDao()
                                .deleteAllTransactionsByQuizId(quizFromDb.id)
                            appDatabase.finishedLevelsDao()
                                .deleteAllFinishedLevelsByQuizId(quizFromDb.id)
                            appDatabase.quizDao().delete(quizFromDb)
                        }
                    }
                    return@map sortedQuizList
                }
                .doOnSuccess { quizzes ->
                    appDatabase
                        .quizDao()
                        .insertQuizesWithQuizTranslationsWithFinishedLevels(
                            quizConverter.convertCollection(
                                quizzes,
                                quizConverter::convert
                            )
                        )
                    val langs = appDatabase.quizTranslationsDao().getAllLangs().toSet()
                    preferences.setLangs(langs)

                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { emitter.onSuccess(Result.success()) }
                .subscribeBy(
                    onSuccess = { Timber.d("onSuccess service: ${it.size} quizzes inserted") },
                    onError = { Timber.e(it, "onError service while update quizzes from server") },
                    onComplete = { Timber.d("onComplete service") }
                ).addTo(compositeDisposable)
        }
    }

    override fun onStopped() {
        super.onStopped()
        compositeDisposable.dispose()
    }
}
