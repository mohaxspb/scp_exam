package ru.kuchanov.scpquiz.mvp.presenter

import android.app.Application
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.mvp.BaseView
import timber.log.Timber
import java.util.*

abstract class BasePresenter<V : BaseView>(
        protected open var appContext: Application,
        open var preferences: MyPreferenceManager,
        protected open var router: ScpRouter,
        protected open var appDatabase: AppDatabase,
        protected open var apiClient: ApiClient
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
        Completable.fromAction {
            with(appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet()) {
                score += Constants.REWARD_VIDEO_ADS
                appDatabase.userDao().update(this).toLong()
            }
        }
                .toSingle {
                    val quizTransaction = QuizTransaction(
                            quizId = null,
                            transactionType = TransactionType.ADV_WATCHED,
                            coinsAmount = Constants.REWARD_VIDEO_ADS
                    )
                    return@toSingle appDatabase.transactionDao().insert(quizTransaction)
                }
                .flatMapCompletable { quizTransactionId ->
                    apiClient.addTransaction(
                            null,
                            TransactionType.ADV_WATCHED,
                            Constants.REWARD_VIDEO_ADS
                    )
                            .doOnSuccess { nwQuizTransaction ->
                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                        quizTransactionId = quizTransactionId,
                                        quizTransactionExternalId = nwQuizTransaction.id)
                                Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransactionId))
                            }
                            .ignoreElement()
                            .onErrorComplete()
                }
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
                            Timber.d("Success transaction from REWARDED VIDEO")
                        }
                )
    }
}