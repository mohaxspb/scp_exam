package ru.kuchanov.scpquiz.mvp.presenter

import android.app.Application
import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.mvp.BaseView
import ru.terrakok.cicerone.Router
import timber.log.Timber

abstract class BasePresenter<V : BaseView>(
    protected open var appContext: Application,
    protected open var preferences: MyPreferenceManager,
    protected open var router: Router
) : MvpPresenter<V>() {

    init {
        Timber.d("constructor: ${javaClass.simpleName}")
    }

    override fun onFirstViewAttach() {
        Timber.d("onFirstViewAttach: ${javaClass.simpleName}")
        super.onFirstViewAttach()
    }

    override fun onDestroy() {
        Timber.d("onDestroy: ${javaClass.simpleName}")
        super.onDestroy()
    }
}