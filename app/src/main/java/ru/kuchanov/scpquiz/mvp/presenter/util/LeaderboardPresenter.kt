package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import android.content.Intent
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.UserLeaderboardViewModel
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.api.NwUser
import ru.kuchanov.scpquiz.mvp.AuthPresenter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.util.LeaderboardView
import ru.kuchanov.scpquiz.ui.fragment.util.LeaderboardFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class LeaderboardPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<LeaderboardView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor), AuthPresenter<LeaderboardFragment> {

    private val userList = mutableListOf<UserLeaderboardViewModel>()

    override lateinit var authDelegate: AuthDelegate<LeaderboardFragment>

    override fun getAuthView(): LeaderboardView = viewState

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        showLeaderboard(Constants.OFFSET_ZERO)
    }

    override fun onAuthSuccess() {
        preferences.setIntroDialogShown(true)
        viewState.showMessage(R.string.settings_success_auth)
        getCurrentPositionInLeaderboard()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }

    fun getLeaderboard(offset: Int) {
        apiClient.getLeaderboard(offset, Constants.LIMIT_PAGE)
                .map {
                    it.map { nwUser ->
                        UserLeaderboardViewModel(
                                name = nwUser.fullName,
                                avatarUrl = nwUser.avatar,
                                score = nwUser.score
                        )
                    }
                }
                .delay(650, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewState.enableScrollListener(false)
                    if (offset > 0) {
                        viewState.showBottomProgress(true)
                    } else {
                        viewState.showSwipeProgressBar(true)
                    }
                }
                .doOnEvent { _, _ ->
                    viewState.enableScrollListener(true)
                    viewState.showBottomProgress(false)
                    viewState.showSwipeProgressBar(false)
                }
                .subscribeBy(
                        onSuccess = {
                            if (offset == 0) {
                                userList.clear()
                            }
                            userList.addAll(it)
                            viewState.showLeaderboard(userList)
                        },
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
                    .doOnSubscribe {
                        //TODO show progressBar - hide UI etc..
                        viewState.showSwipeProgressBar(true) }
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

    fun showLeaderboard(offset: Int) {
        getLeaderboard(offset)
        getCurrentPositionInLeaderboard()
    }

    fun onBackClicked() {
        router.exit()
    }
}