package com.scp.scpexam.mvp.presenter

import android.app.Application
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.MvpPresenter
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.controller.navigation.ScpRouter
import com.scp.scpexam.model.db.TransactionType
import com.scp.scpexam.model.db.UserRole
import com.scp.scpexam.mvp.BaseView
import timber.log.Timber

abstract class BasePresenter<V : BaseView>(
        protected open var appContext: Application,
        open var preferences: MyPreferenceManager,
        protected open var router: ScpRouter,
        protected open var appDatabase: AppDatabase,
        protected open var apiClient: ApiClient,
        protected open var transactionInteractor: TransactionInteractor
) : MvpPresenter<V>() {

    val compositeDisposable = CompositeDisposable()

    init {
        Timber.d("constructor: ${javaClass.simpleName}")
    }

    override fun onFirstViewAttach() {
        Timber.d("onFirstViewAttach: ${javaClass.simpleName}")
        super.onFirstViewAttach()
    }

    override fun onDestroy() {
        Timber.d("onDestroy: ${javaClass.simpleName}")
        compositeDisposable.clear()
        super.onDestroy()
    }

    fun onRewardedVideoFinished() {
        Timber.d("onRewardedVideoFinished")
        compositeDisposable.add(Completable.fromAction {
            with(appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet()) {
                score += Constants.REWARD_VIDEO_ADS
                appDatabase.userDao().update(this).toLong()
            }
        }
                .andThen(transactionInteractor.makeTransaction(null, TransactionType.ADV_WATCHED, Constants.REWARD_VIDEO_ADS))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            Timber.e(it)
                            viewState.showMessage(it.message
                                    ?: "Unexpected error")
                        },
                        onComplete = {
                            viewState.showMessage(appContext.getString(R.string.coins_received, Constants.REWARD_VIDEO_ADS))
//                            Timber.d("Success transaction from REWARDED VIDEO")
                        }
                ))
    }
}