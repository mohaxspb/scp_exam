package com.scp.scpexam.mvp

import android.content.Intent
import androidx.fragment.app.Fragment
import com.facebook.login.LoginManager
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.Constants
import moxy.viewstate.strategy.AddToEndSingleStrategy

interface AuthView : BaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun startGoogleLogin(signInIntent: Intent, fragment: Fragment) {
        fragment.startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE)
    }

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun startVkLogin(fragment: Fragment) {
        VKSdk.login(fragment.activity!!, VKScope.EMAIL)
    }

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun startFacebookLogin(fragment: Fragment) {
        LoginManager.getInstance().logInWithReadPermissions(fragment, Constants.Auth.FACEBOOK_SCOPES)
    }
}

const val REQUEST_CODE_GOOGLE = 11