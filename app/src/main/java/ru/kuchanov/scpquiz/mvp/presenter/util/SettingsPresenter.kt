package ru.kuchanov.scpquiz.mvp.presenter.util

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.SettingsView
import javax.inject.Inject

@InjectViewState
class SettingsPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    private var appDatabase: AppDatabase
) : BasePresenter<SettingsView>(appContext, preferences, router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        //todo
        viewState.showLang(preferences.getLang())
    }
}