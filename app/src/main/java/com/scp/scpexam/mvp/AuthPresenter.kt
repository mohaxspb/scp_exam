package com.scp.scpexam.mvp

import androidx.viewbinding.ViewBinding
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.utils.AuthDelegate

interface AuthPresenter<T : BaseFragment<out AuthView, out BasePresenter<out AuthView>, out ViewBinding>> {

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

    fun onAuthError()
}