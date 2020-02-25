package ru.kuchanov.scpquiz.mvp

import android.content.Intent
import androidx.fragment.app.Fragment
import com.facebook.login.LoginManager
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.Constants

interface AuthView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun startGoogleLogin(signInIntent: Intent, fragment: Fragment) {
        fragment.startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE)
    }

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun startVkLogin(fragment: Fragment) {
        VKSdk.login(fragment.activity!!, VKScope.EMAIL)
    }

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun startFacebookLogin(fragment: Fragment) {
        LoginManager.getInstance().logInWithReadPermissions(fragment, Constants.Auth.FACEBOOK_SCOPES)
    }
}

const val REQUEST_CODE_GOOGLE = 11