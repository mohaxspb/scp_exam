package ru.kuchanov.scpquiz.mvp.presenter.intro

import android.app.Application
import android.content.Intent
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.mvp.AuthPresenter
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.intro.IntroDialogView
import ru.kuchanov.scpquiz.ui.fragment.intro.IntroDialogFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class IntroDialogPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<IntroDialogView>(
        appContext,
        preferences,
        router,
        appDatabase,
        apiClient,
        transactionInteractor
), AuthPresenter<IntroDialogFragment> {

    override fun onAuthSuccess() {
        Single
                .fromCallable {
                    appDatabase.finishedLevelsDao().getCountWhereLevelAvailableTrueFinishedLevels() > 5
                            || appDatabase.finishedLevelsDao().getCountWhereSomethingExceptLevelAvailableTrueFinishedLevels() > 0
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { hasAnyGameAction ->
                            if (hasAnyGameAction) {
                                navigateToAllQuizScreen()
                            } else {
                                navigateToFirstLevel()
                            }
                        },
                        onError = { Timber.e(it) }
                )
    }

    override fun onAuthCanceled() {
        navigateToFirstLevel()
        viewState.showMessage(R.string.canceled_auth)
    }

    override fun onAuthError() {
        viewState.showMessage(appContext.getString(R.string.auth_retry))
        viewState.showChatActions(generateAuthActions(),ChatActionsGroupType.AUTH)
    }

    override lateinit var authDelegate: AuthDelegate<IntroDialogFragment>

    override fun getAuthView(): IntroDialogView = viewState

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
    }

    private fun generateStartGameActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageOk = appContext.getString(R.string.chat_action_sure)
        chatActions += ChatAction(
                messageOk,
                onActionClicked(messageOk) {
                    preferences.setIntroDialogShown(true)
                    showAuthChatActions()
                },
                R.drawable.selector_chat_action_accent
        )
        val messageSure = appContext.getString(R.string.chat_action_yes)
        chatActions += ChatAction(
                messageSure,
                onActionClicked(messageSure) {
                    preferences.setIntroDialogShown(true)
                    showAuthChatActions()
                },
                R.drawable.selector_chat_action_green
        )

        return chatActions
    }

    private fun generateAuthActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageAuthFacebook = appContext.getString(R.string.chat_action_auth_facebook)
        chatActions += ChatAction(
                messageAuthFacebook,
                onActionClicked(messageAuthFacebook) { onFacebookLoginClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageAuthGoogle = appContext.getString(R.string.chat_action_auth_google)
        chatActions += ChatAction(
                messageAuthGoogle,
                onActionClicked(messageAuthGoogle) { onGoogleLoginClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageAuthVk = appContext.getString(R.string.chat_action_auth_vk)
        chatActions += ChatAction(
                messageAuthVk,
                onActionClicked(messageAuthVk) { onVkLoginClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageSkipAuth = appContext.getString(R.string.chat_action_skip_auth)
        chatActions += ChatAction(
                messageSkipAuth,
                onActionClicked(messageSkipAuth) { navigateToFirstLevel() },
                R.drawable.selector_chat_action_accent
        )

        return chatActions
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

    private fun navigateToFirstLevel() {
        Single.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            router.newRootScreen(Constants.Screens.GameScreen(quiz.id))
//                                    QuizScreenLaunchData(quiz.id, true)
                        }
                )
    }

    private fun navigateToAllQuizScreen() {
        Single.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            router.newRootScreen(Constants.Screens.LevelsScreen)
                        }
                )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }
}