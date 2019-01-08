package ru.kuchanov.scpquiz.mvp.presenter.activity

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.activity.MainView
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient
) : BasePresenter<MainView>(appContext, preferences, router, appDatabase, apiClient) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        router.newRootScreen(Constants.Screens.ENTER)
    }
}
