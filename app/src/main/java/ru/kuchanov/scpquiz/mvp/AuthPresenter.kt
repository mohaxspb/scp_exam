package ru.kuchanov.scpquiz.mvp

import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate

interface AuthPresenter<T : BaseFragment<out AuthView, out BasePresenter<out AuthView>>> {
    var authDelegate: AuthDelegate<T>

//    fun getAuthView(): AuthView

    fun onFacebookLoginClicked()
    fun onVkLoginClicked()
    fun onGoogleLoginClicked()
    fun onAuthSuccess()
}