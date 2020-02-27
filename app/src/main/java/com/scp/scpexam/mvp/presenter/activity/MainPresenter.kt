package com.scp.scpexam.mvp.presenter.activity

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import moxy.InjectViewState
import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.activity.MainView
import com.scp.scpexam.receivers.AutoSyncReceiver.Companion.setAlarm
import com.scp.scpexam.services.DownloadService
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: Router,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<MainView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        setAlarm(appContext)
        router.newRootScreen(Constants.Screens.EnterScreen)

        val downloadServiceIntent = Intent(appContext, DownloadService::class.java)
        ContextCompat.startForegroundService(appContext, downloadServiceIntent)
    }
}
