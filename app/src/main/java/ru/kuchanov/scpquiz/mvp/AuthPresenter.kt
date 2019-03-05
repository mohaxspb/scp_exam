package ru.kuchanov.scpquiz.mvp

import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate

interface AuthPresenter<T : BaseFragment<out AuthView, out BasePresenter<out AuthView>>> {

    var authDelegate: AuthDelegate<T>

    fun getAuthView(): AuthView

    fun onFacebookLoginClicked() {
        getAuthView().startFacebookLogin(authDelegate.getFragment())
    }

    fun onVkLoginClicked() {
        getAuthView().startVkLogin(authDelegate.getFragment())
    }

    fun onGoogleLoginClicked() {
        authDelegate.startGoogleLogin()
    }

    fun onAuthSuccess()

    fun onAuthCanceled()
}