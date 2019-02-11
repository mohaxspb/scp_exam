package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.UserLeaderboardViewModel
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.api.NwUser
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.util.LeaderboardView
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class LeaderboardPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<LeaderboardView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        showLeaderboard()
    }

    private fun getLeaderboard() {
        apiClient.getLeaderboard(0, 50)
                .map {
                    it.map { nwUser ->
                        UserLeaderboardViewModel(
                                name = nwUser.fullName,
                                avatarUrl = nwUser.avatar,
                                score = nwUser.score
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showSwipeProgressBar(true) }
                .doOnEvent { _, _ -> viewState.showSwipeProgressBar(false) }
                .subscribeBy(
                        onSuccess = { viewState.showLeaderboard(it) },
                        onError = {
                            Timber.e(it)
                            viewState.showMessage(it.toString())
                        }
                )
                .addTo(compositeDisposable)
    }

    private fun getCurrentPositionInLeaderboard() {
        if (preferences.getTrueAccessToken() != null) {
            Single.zip(
                    apiClient.getNwUser(),
                    apiClient.getCurrentPositionInLeaderboard(),
                    BiFunction { nwUser: NwUser, position: Int ->
                        nwUser to position
                    }
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { viewState.showSwipeProgressBar(true) }
                    .doOnEvent { _, _ -> viewState.showSwipeProgressBar(false) }
                    .subscribeBy(
                            onSuccess = { viewState.showUserPosition(it.first, it.second) },
                            onError = {
                                Timber.e(it)
                                viewState.showMessage(it.toString())
                            }
                    )
                    .addTo(compositeDisposable)
        }
    }

    fun showLeaderboard() {
        getLeaderboard()
        getCurrentPositionInLeaderboard()
    }
}