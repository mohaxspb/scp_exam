package ru.kuchanov.scpquiz.ui.utils

import android.content.Intent
import android.support.v4.app.FragmentActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.gson.GsonBuilder
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.model.VKApiUserFull
import com.vk.sdk.api.model.VKList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.model.CommonUserData
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.mvp.AuthPresenter
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.ui.BaseFragment
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

@SuppressWarnings("Injectable")
class AuthDelegate<T : BaseFragment<out AuthView, out BasePresenter<out AuthView>>>(
        private val fragment: T,
        private val authPresenter: AuthPresenter<*>,
        private var apiClient: ApiClient,
        internal var preferences: MyPreferenceManager
) {
    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    @Inject
    lateinit var transactionInteractor: TransactionInteractor
    @Inject
    lateinit var appDatabase: AppDatabase
    @Inject
    lateinit var quizConverter: QuizConverter
    private val compositeDisposable = CompositeDisposable()
    private val callbackManager = CallbackManager.Factory.create()
    private var googleApiClient: GoogleApiClient? = null

    fun getFragment(): BaseFragment<out AuthView, out BasePresenter<out AuthView>> {
        return fragment
    }

    fun onViewCreated(fragmentActivity: FragmentActivity) {
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
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val vkCallback = object : VKCallback<VKAccessToken> {
            override fun onResult(vkAccessToken: VKAccessToken) {
                val commonUserData = CommonUserData().apply {
                    email = vkAccessToken.email
                    id = vkAccessToken.userId
                }
                val request = VKApi.users().get()
                request.executeWithListener(object : VKRequest.VKRequestListener() {
                    override fun onComplete(response: VKResponse?) {
                        @Suppress("UNCHECKED_CAST")
                        val user = (response!!.parsedModel as VKList<VKApiUserFull>)[0]
                        commonUserData.firstName = user.first_name
                        commonUserData.lastName = user.last_name
                        commonUserData.avatarUrl = user.photo_200
                        commonUserData.fullName = user.first_name + "" + user.last_name
                        val builder = GsonBuilder()
                        val gson = builder.create()
                        socialLogin(Constants.Social.VK, gson.toJson(commonUserData))
                    }

                    override fun onError(error: VKError?) {
                        Timber.e(error!!.errorMessage)
                    }

                    override fun attemptFailed(request: VKRequest?, attemptNumber: Int, totalAttempts: Int) {
                        fragment.showMessage("VK failed $request attemptNumber: $attemptNumber totalAttempts: $totalAttempts")
                    }
                })
            }

            override fun onError(error: VKError) {
                Timber.d("Error: $error")
            }
        }

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, vkCallback)) {
            when (requestCode) {
                REQUEST_CODE_GOOGLE -> {
                    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
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
        compositeDisposable.add(apiClient.socialLogin(socialName, data!!)
                .doOnSuccess { (accessToken, refreshToken) ->
                    preferences.setAccessToken(accessToken)
                    preferences.setRefreshToken(refreshToken)
                }
                .flatMap { it ->
                    apiClient.getNwUser()
                            .doOnSuccess { nwUser ->
                                appDatabase.userDao().getOneByRole(UserRole.PLAYER)
                                        .map { it ->
                                            it.name = nwUser.fullName!!
                                            it.avatarUrl = nwUser.avatar
                                            it.score = nwUser.score
                                            appDatabase.userDao().update(it)
                                        }
                            }
                }
                .flatMap {
                    apiClient.getNwQuizTransactionList()
                            .doOnSuccess { nwTransactionList ->
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
                        }
                )
        )
    }

    fun onPause() {
        if (googleApiClient != null) {
            googleApiClient!!.stopAutoManage(fragment.getActivity()!!)
            googleApiClient!!.disconnect()
        }
    }

    companion object {
        private const val REQUEST_CODE_GOOGLE = 11
    }
}