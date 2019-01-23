package ru.kuchanov.scpquiz.mvp.presenter.activity

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.activity.MainView
import ru.kuchanov.scpquiz.receivers.AutoSyncReceiver
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<MainView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        appContext.let { AutoSyncReceiver.setAlarm(it) }
        router.newRootScreen(Constants.Screens.ENTER)
    }
}
