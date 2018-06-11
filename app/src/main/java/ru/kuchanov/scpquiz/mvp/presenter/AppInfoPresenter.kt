package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.mvp.view.AppInfoView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class AppInfoPresenter @Inject constructor(
    private var appDatabase: AppDatabase,
    private var router: Router
) : MvpPresenter<AppInfoView>() {

    init {
        Timber.d("constructor")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    fun onSomethingClick() = viewState.showMessage("onSomethingClick from Fragment")
}