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
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.mvp.BaseView
import timber.log.Timber

abstract class BasePresenter<V : BaseView>(
        protected open var appContext: Application,
        open var preferences: MyPreferenceManager,
        protected open var router: ScpRouter,
        protected open var appDatabase: AppDatabase
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { viewState.showMessage(appContext.getString(R.string.coins_received, Constants.REWARD_VIDEO_ADS)) }
    }
}