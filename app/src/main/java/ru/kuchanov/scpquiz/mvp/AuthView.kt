package ru.kuchanov.scpquiz.mvp

import android.content.Intent
import android.support.v4.app.Fragment
import com.facebook.login.LoginManager
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import ru.kuchanov.scpquiz.Constants

interface AuthView : BaseView {

//    fun showMessage(message: String)
    fun startGoogleLogin(signInIntent: Intent, fragment: Fragment) {
        fragment.startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE)
    }

    fun startVkLogin(fragment: Fragment) {
        VKSdk.login(fragment.activity!!, VKScope.EMAIL)
    }

    fun startFacebookLogin(fragment: Fragment) {
        LoginManager.getInstance().logInWithReadPermissions(fragment, Constants.Auth.FACEBOOK_SCOPES)
    }
}

const val REQUEST_CODE_GOOGLE = 11