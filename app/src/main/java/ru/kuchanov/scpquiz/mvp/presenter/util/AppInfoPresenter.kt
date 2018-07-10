package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.util.AppInfoView
import javax.inject.Inject

@InjectViewState
class AppInfoPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    override var appDatabase: AppDatabase
) : BasePresenter<AppInfoView>(appContext, preferences, router, appDatabase) {

    fun onSomethingClick() = viewState.showMessage("onSomethingClick from Fragment")
}