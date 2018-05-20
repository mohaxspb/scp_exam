package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.mvp.view.AppInfoView
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

@InjectViewState
class AppInfoPresenter @Inject constructor() : MvpPresenter<AppInfoView>() {

    init {
        Timber.d("constructor")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    fun onSomethingClick() = viewState.showMessage("onSomethingClick from Fragment")
}