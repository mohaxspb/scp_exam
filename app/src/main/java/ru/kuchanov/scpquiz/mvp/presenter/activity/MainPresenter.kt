package ru.kuchanov.scpquiz.mvp.presenter.activity

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.activity.MainView
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
        var apiClient: ApiClient,
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase
) : BasePresenter<MainView>(appContext, preferences, router, appDatabase) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        router.navigateTo(Constants.Screens.ENTER)
        apiClient.getNwQuizList().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(onError = { it: Throwable ->
            Timber.e(it)
        })
    }
}

