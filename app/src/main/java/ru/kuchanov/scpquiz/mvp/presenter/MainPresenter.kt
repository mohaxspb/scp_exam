package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
    private val router: Router
) : MvpPresenter<MainView>() {

    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Timber.d("onFirstViewAttach")

        router.navigateTo(Constants.Screens.ENTER)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}