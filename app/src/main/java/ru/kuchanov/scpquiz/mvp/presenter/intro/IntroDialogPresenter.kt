package ru.kuchanov.scpquiz.mvp.presenter.intro

import android.app.Application
import android.content.Intent
import com.arellomobile.mvp.InjectViewState
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.QuizApi
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.model.ui.QuizScreenLaunchData
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.intro.IntroDialogView
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class IntroDialogPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase
) : BasePresenter<IntroDialogView>(appContext, preferences, router, appDatabase) {

    //auth
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()

    private lateinit var doctor: User

    private lateinit var player: User

    private lateinit var quiz: Quiz

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        Flowable
                .zip(
                        appDatabase.userDao().getOneByRole(UserRole.DOCTOR).toFlowable(),
                        appDatabase.userDao().getOneByRole(UserRole.PLAYER).toFlowable(),
                        appDatabase.quizDao().getFirst().toFlowable(),
                        Function3 { doctor: User, player: User, firstLevel: Quiz -> Triple(doctor, player, firstLevel) }
                )
                .doOnNext {
                    doctor = it.first
                    player = it.second
                    quiz = it.third
                }
                .flatMap {
                    Flowable.intervalRange(
                            0,
                            3,
                            1,
                            2,
                            TimeUnit.SECONDS
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            val array = appContext.resources.getStringArray(R.array.intro_dialog_texts)
                            var message = array[it.toInt()]
                            if (message.contains("%s")) {
                                message = message.replace("%s", player.name)
                            }
                            viewState.showChatMessage(
                                    message,
                                    doctor
                            )
                        },
                        onComplete = {
                            viewState.showChatActions(generateStartGameActions(), ChatActionsGroupType.START_GAME)
                        }
                )

        initFacebook()
    }

    private fun initFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Timber.d("LOGIN FB RESULT : %s", loginResult.accessToken.token)
                        //todo
//                        apiClient.loginSocial(Constants.FACEBOOK, loginResult.accessToken.token)
//                                .doOnSuccess({ tokenResponse ->
//                                    preferences.setAccessToken(tokenResponse.accessToken)
//                                    preferences.setRefreshToken(tokenResponse.refreshToken)
//                                })
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribeBy(
//                                        { tokenResponse -> router.navigateTo(Constants.ALL_QUIZ_SCREEN) },
//                                        { error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show() }
//                                )

                    }

                    override fun onCancel() {}

                    override fun onError(exception: FacebookException) {
                        Timber.d("ON ERROR FB :%s", exception.toString())
                    }
                })
    }

    private fun generateStartGameActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageOk = appContext.getString(R.string.chat_action_sure)
        chatActions += ChatAction(
                messageOk,
                onActionClicked(messageOk) { showAuthChatActions() },
                R.drawable.selector_chat_action_accent
        )
        val messageSure = appContext.getString(R.string.chat_action_yes)
        chatActions += ChatAction(
                messageSure,
                onActionClicked(messageSure) { showAuthChatActions() },
                R.drawable.selector_chat_action_green
        )

        return chatActions
    }

    private fun generateAuthActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageAuthFacebook = appContext.getString(R.string.chat_action_auth_facebook)
        chatActions += ChatAction(
                messageAuthFacebook,
                onActionClicked(messageAuthFacebook) { onAuthFacebookClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageAuthGoogle = appContext.getString(R.string.chat_action_auth_google)
        chatActions += ChatAction(
                messageAuthGoogle,
                onActionClicked(messageAuthGoogle) { onAuthGoogleClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageAuthVk = appContext.getString(R.string.chat_action_auth_vk)
        chatActions += ChatAction(
                messageAuthVk,
                onActionClicked(messageAuthVk) { onAuthVkClicked() },
                R.drawable.selector_chat_action_accent
        )

        return chatActions
    }

    private fun onAuthVkClicked() {
        Timber.d("onAuthVkClicked")
        //todo
    }

    private fun onAuthGoogleClicked() {
        Timber.d("onAuthGoogleClicked")
        //todo
    }

    private fun onAuthFacebookClicked() {
        Timber.d("onAuthFacebookClicked")
        viewState.startFacebookLogin()
    }

    private fun onActionClicked(text: String, onCompleteAction: () -> Unit): (Int) -> Unit =
            { index ->
                viewState.removeChatAction(index)
                viewState.showChatMessage(text, player)

                onCompleteAction.invoke()
            }

    private fun showAuthChatActions() {
        viewState.showChatMessage(appContext.getString(R.string.message_auth_suggestion), doctor)
        viewState.showChatActions(generateAuthActions(), ChatActionsGroupType.AUTH)
    }

    private fun getOkActionForText(text: String): (Int) -> Unit =
            { index ->
                //todo move to auth actions
//                preferences.setIntroDialogShown(true)

                viewState.removeChatAction(index)
                viewState.showChatMessage(text, player)

                //todo move to auth actions
//                navigateToFirstLevel()
            }

    private fun navigateToFirstLevel() {
        Single.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            router.newRootScreen(
                                    Constants.Screens.QUIZ,
                                    QuizScreenLaunchData(quiz.id, true)
                            )
                        }
                )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}