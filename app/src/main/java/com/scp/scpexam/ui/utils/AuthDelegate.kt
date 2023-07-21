package com.scp.scpexam.ui.utils

import android.app.Activity
import android.content.Intent
import androidx.viewbinding.ViewBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import com.scp.scpexam.model.CommonUserData
import com.scp.scpexam.model.api.QuizConverter
import com.scp.scpexam.mvp.AuthPresenter
import com.scp.scpexam.mvp.AuthView
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.ui.BaseFragment
import com.squareup.moshi.Moshi
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.requests.VKRequest
import com.vk.sdk.api.users.UsersService
import com.vk.sdk.api.users.dto.UsersUserFullDto
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

@SuppressWarnings("Injectable")
class AuthDelegate<T : BaseFragment<out AuthView, out BasePresenter<out AuthView>, out ViewBinding>>(
    private val fragment: T,
    private val authPresenter: AuthPresenter<*>,
    private var apiClient: ApiClient,
    internal var preferences: MyPreferenceManager
) {

    companion object {
        private const val REQUEST_CODE_GOOGLE = 11
    }

    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    val authLauncher = fragment.registerForActivityResult(
        VK.getVKAuthActivityResultContract()
    ) {
        when (it) {
            is VKAuthenticationResult.Success -> {
                // TODO перенести сюда
                Timber.d("Успешная авторизация ${it.token}")
                val commonUserData = CommonUserData().apply {
                    email = it.token.email
                    id = it.token.userId.value.toString()
                }
                VK.execute(VKUsersCommand(), object: VKApiCallback<List<VKUser>> {
                    override fun success(result: List<VKUser>) {
//                        @Suppress("UNCHECKED_CAST")
                        val user = result[0]
                        commonUserData.firstName = user.firstName
                        commonUserData.lastName = user.lastName
                        commonUserData.avatarUrl = user.avatarUrl
                        commonUserData.fullName = user.fullName
                        val jsonAdapter = moshi.adapter(CommonUserData::class.java)
                        socialLogin(Constants.Social.VK, jsonAdapter.toJson(commonUserData))
                    }
                    override fun fail(error: Exception) {
                        Timber.e(error, "ошибка ВК авторизации")
                        fragment.showMessage("VK failed $error")
                    }
                })
//                VK.execute(UsersService().usersGet(), object: VKApiCallback<List<UsersUserFullDto>> {
//                    override fun success(result: List<UsersUserFullDto>) {
//                        @Suppress("UNCHECKED_CAST")
//                        val user = result[0]
//                        commonUserData.firstName = user.firstName
//                        commonUserData.lastName = user.lastName
//                        commonUserData.avatarUrl = user.photo200
//                        commonUserData.fullName = user.firstName + "" + user.lastName
//                        val jsonAdapter = moshi.adapter(CommonUserData::class.java)
//                        socialLogin(Constants.Social.VK, jsonAdapter.toJson(commonUserData))
//                    }
//                    override fun fail(error: Exception) {
//                        Timber.e(error, "ошибка ВК авторизации")
//                        fragment.showMessage("VK failed $request")
//                    }
//                })
//                VK.execute(request, object : VKApiCallback<List<UsersUserFullDto>> {
//                    override fun fail(error: Exception) {
//                        Timber.e(error, "ошибка ВК авторизации")
//                        fragment.showMessage("VK failed $request")
//                    }
//
//                    override fun success(result: List<UsersUserFullDto>) {
//                        @Suppress("UNCHECKED_CAST")
//                        val user = result[0]
//                        commonUserData.firstName = user.firstName
//                        commonUserData.lastName = user.lastName
//                        commonUserData.avatarUrl = user.photo200
//                        commonUserData.fullName = user.firstName + "" + user.lastName
//                        val jsonAdapter = moshi.adapter(CommonUserData::class.java)
//                        socialLogin(Constants.Social.VK, jsonAdapter.toJson(commonUserData))
//                    }
//
//                })

//                val request = VK.users().get()
//                request.executeWithListener(object : VKRequest.VKRequestListener() {
//                    override fun onComplete(response: VKResponse?) {
//                        @Suppress("UNCHECKED_CAST")
//                        val user = (response!!.parsedModel as VKList<VKApiUserFull>)[0]
//                        commonUserData.firstName = user.first_name
//                        commonUserData.lastName = user.last_name
//                        commonUserData.avatarUrl = user.photo_200
//                        commonUserData.fullName = user.first_name + "" + user.last_name
//                        val jsonAdapter = moshi.adapter(CommonUserData::class.java)
//                        socialLogin(Constants.Social.VK, jsonAdapter.toJson(commonUserData))
//                    }
//
//                    override fun onError(error: VKError?) {
//                        Timber.e(error!!.errorMessage)
//                    }
//
//                    override fun attemptFailed(
//                        request: VKRequest?,
//                        attemptNumber: Int,
//                        totalAttempts: Int
//                    ) {
//                        fragment.showMessage("VK failed $request attemptNumber: $attemptNumber totalAttempts: $totalAttempts")
//                    }
//                })
            }
            is VKAuthenticationResult.Failed -> {
                // TODO перенести сюда
                Timber.e(it.exception, "Ошибка ${it.exception.authError}")
            }
        }
    }

    @Inject
    lateinit var transactionInteractor: TransactionInteractor

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var quizConverter: QuizConverter

    @Inject
    lateinit var moshi: Moshi

    private val compositeDisposable = CompositeDisposable()

    private val callbackManager = CallbackManager.Factory.create()

    private var googleApiClient: GoogleApiClient? = null

    fun getFragment(): BaseFragment<out AuthView, out BasePresenter<out AuthView>, out ViewBinding> {
        return fragment
    }

    fun onViewCreated(fragmentActivity: androidx.fragment.app.FragmentActivity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.SERVER_GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        googleApiClient = GoogleApiClient.Builder(fragmentActivity)
            .enableAutoManage(fragmentActivity) { Timber.d("ERROR") }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        fbRegisterCallback()
    }

    fun startGoogleLogin() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient!!)
        (fragment as AuthView).startGoogleLogin(signInIntent, fragment)
    }

    private fun fbRegisterCallback() {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    socialLogin(Constants.Social.FACEBOOK, loginResult.accessToken.token)
                }

                override fun onCancel() {}

                override fun onError(exception: FacebookException) {
                    Timber.d("ON ERROR FB :%s", exception.toString())
                }
            })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        TODO перенести куда-то
//        val vkCallback = object : VKCallback<VKAccessToken> {
//            override fun onResult(vkAccessToken: VKAccessToken) {
//                val commonUserData = CommonUserData().apply {
//                    email = vkAccessToken.email
//                    id = vkAccessToken.userId
//                }
//                val request = VKApi.users().get()
//                request.executeWithListener(object : VKRequest.VKRequestListener() {
//                    override fun onComplete(response: VKResponse?) {
//                        @Suppress("UNCHECKED_CAST")
//                        val user = (response!!.parsedModel as VKList<VKApiUserFull>)[0]
//                        commonUserData.firstName = user.first_name
//                        commonUserData.lastName = user.last_name
//                        commonUserData.avatarUrl = user.photo_200
//                        commonUserData.fullName = user.first_name + "" + user.last_name
//                        val jsonAdapter = moshi.adapter(CommonUserData::class.java)
//                        socialLogin(Constants.Social.VK, jsonAdapter.toJson(commonUserData))
//                    }
//
//                    override fun onError(error: VKError?) {
//                        Timber.e(error!!.errorMessage)
//                    }
//
//                    override fun attemptFailed(
//                        request: VKRequest?,
//                        attemptNumber: Int,
//                        totalAttempts: Int
//                    ) {
//                        fragment.showMessage("VK failed $request attemptNumber: $attemptNumber totalAttempts: $totalAttempts")
//                    }
//                })
//            }
//
//            override fun onError(error: VKError) {
//                Timber.d("Error: $error")
//            }
//        }
        Timber.d("onActivityResult: $resultCode")
        if (resultCode != Activity.RESULT_OK) {
            authPresenter.onAuthCanceled()
        } else {
            when (requestCode) {
                REQUEST_CODE_GOOGLE -> {
                    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)!!
                    Timber.d("result: ${result.isSuccess}/${result.signInAccount}")
                    if (result.isSuccess) {
                        socialLogin(Constants.Social.GOOGLE, result.signInAccount!!.idToken)
                    } else {
                        Timber.e("ERROR : %s", result.status)
                    }
                }
                else -> callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun socialLogin(socialName: Constants.Social, data: String?) {
        compositeDisposable.add(
            apiClient.socialLogin(socialName, data!!)
                .doOnSuccess { (accessToken, refreshToken) ->
                    preferences.setTrueAccessToken(accessToken)
                    preferences.setRefreshToken(refreshToken)
                }
                .flatMap {
                    apiClient.getNwQuizTransactionList()
                        .map { nwTransactionList ->
                            nwTransactionList.forEach { nwQuizTransaction ->
                                appDatabase.transactionDao().insert(
                                    quizConverter.convert(nwQuizTransaction)
                                )
                            }
                        }
                }
                .flatMapCompletable { transactionInteractor.syncAllProgress() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = { authPresenter.onAuthSuccess() },
                    onError = {
                        Timber.e(it)
                        fragment.showMessage(it.toString())
                        authPresenter.onAuthError()
                    }
                )
        )
    }

    fun onPause() {
        if (googleApiClient != null) {
            googleApiClient!!.stopAutoManage(fragment.requireActivity())
            googleApiClient!!.disconnect()
        }
    }

    fun startVkLogin() {
        authLauncher.launch(arrayListOf(VKScope.EMAIL))
    }
}