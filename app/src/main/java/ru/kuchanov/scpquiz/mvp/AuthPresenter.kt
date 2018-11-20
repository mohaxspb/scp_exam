package ru.kuchanov.scpquiz.mvp

import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import timber.log.Timber

interface AuthPresenter<T : BaseFragment<out AuthView, out BasePresenter<out AuthView>>> {

    var authDelegate: AuthDelegate<T>

    fun getAuthView(): AuthView

    fun onFacebookLoginClicked() {
        Timber.d("onAuthFBClicked")
        getAuthView().startFacebookLogin(authDelegate.getFragment())
    }

    fun onVkLoginClicked() {
        Timber.d("onAuthVkClicked")
        getAuthView().startVkLogin(authDelegate.getFragment())
    }

    fun onGoogleLoginClicked() {
        Timber.d("onAuthGoogleClicked")
        authDelegate.startGoogleLogin()
    }

    fun onAuthSuccess()
}