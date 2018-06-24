package ru.kuchanov.scpquiz.mvp.presenter.activity

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter
) : BasePresenter<MainView>(appContext, preferences, router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        router.navigateTo(Constants.Screens.ENTER)
    }
}