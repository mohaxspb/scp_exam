package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.AppInfoView
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class AppInfoPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter
) : BasePresenter<AppInfoView>(appContext, preferences, router) {

    fun onSomethingClick() = viewState.showMessage("onSomethingClick from Fragment")
}