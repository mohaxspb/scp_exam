package com.scp.scpexam.mvp.presenter.util

import android.app.Application
import android.content.Intent
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.viewmodel.LeaderboardViewModel
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.model.api.NwUser
import com.scp.scpexam.mvp.AuthPresenter
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.util.LeaderboardView
import com.scp.scpexam.ui.fragment.util.LeaderboardFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class LeaderboardPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: Router,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<LeaderboardView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor), AuthPresenter<LeaderboardFragment> {
    private val userList = mutableListOf<LeaderboardViewModel>()

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

    override fun onAuthCanceled() {
        viewState.showMessage(R.string.canceled_auth)
    }

    override fun onAuthError() {
        viewState.showMessage(appContext.getString(R.string.auth_retry))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }

    fun getLeaderboard(offset: Int) {
        apiClient.getLeaderboard(offset, Constants.LIMIT_PAGE)
                .map {
                    it.map { nwUser ->
                        LeaderboardViewModel(
                                name = nwUser.fullName,
                                avatarUrl = nwUser.avatar,
                                score = nwUser.score,
                                fullCompleteLevels = nwUser.fullCompleteLevels,
                                partCompleteLevels = nwUser.partCompleteLevels
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

    fun getCurrentPositionInLeaderboard() {
        if (preferences.getTrueAccessToken() != null) {
            Single.zip(
                    apiClient.getUserForLeaderboard(),
                    apiClient.getCurrentPositionInLeaderboard(),
                    BiFunction { nwUser: NwUser, position: Int ->
                        nwUser to position
                    }
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        viewState.showCurrentUserUI(false)
                        viewState.showCurrentUserProgressBar(true)
                        viewState.showRetryButton(false)
                    }
                    .doOnEvent { _, _ ->
                        viewState.showCurrentUserProgressBar(false)
                    }
                    .subscribeBy(
                            onSuccess = {
                                viewState.showRetryButton(false)
                                viewState.showCurrentUserUI(true)
                                viewState.showUserPosition(it.first, it.second)
                            },
                            onError = {
                                viewState.showRetryButton(true)
                                viewState.showCurrentUserUI(false)
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