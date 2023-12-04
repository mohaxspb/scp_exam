package com.scp.scpexam.services

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

class PeriodicallySyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    companion object {
        const val PERIODICALLY_SYNC_PERIODIC_WORKER_ID = "PERIODICALLY_SYNC_PERIODIC_WORKER_ID"
        const val PERIODICALLY_SYNC_ONE_TIME_WORKER_ID = "PERIODICALLY_SYNC_ONE_TIME_WORKER_ID"
    }

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var transactionInteractor: TransactionInteractor

    @Inject
    lateinit var preferences: MyPreferenceManager


    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    override fun createWork(): Single<Result> {
        Timber.d("Worker started")
        return Single.create { emitter ->
            Single.fromCallable { preferences.getTrueAccessToken() != null }
                .flatMapCompletable {
                    if (it) {
                        transactionInteractor.syncAllProgress()
                    } else {
                        Completable.complete()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        Timber.d("SYnc from worker complete")
                        emitter.onSuccess(Result.success())
                    },
                    onError = {
                        Timber.e(it, "SYnc from worker error")
                        emitter.onSuccess(Result.failure())
                    }
                ).addTo(compositeDisposable)
        }
    }

    override fun onStopped() {
        super.onStopped()
        compositeDisposable.dispose()
    }
}
