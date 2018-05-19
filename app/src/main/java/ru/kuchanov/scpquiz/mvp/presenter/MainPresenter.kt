package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor() : MvpPresenter<MainView>() {

    init {
        Timber.d("constructor")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}