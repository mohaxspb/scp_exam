package ru.kuchanov.scpquiz.mvp.presenter.intro

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.intro.IntroDialogView
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class IntroDialogPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    override var appDatabase: AppDatabase
) : BasePresenter<IntroDialogView>(appContext, preferences, router, appDatabase) {

    private lateinit var doctor: User

    private lateinit var player: User

    private lateinit var quiz: Quiz

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        Flowable.zip(
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
                .flatMap { _ ->
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
                        viewState.showChatActions(generateStartGameActions())
                    }
                )
    }

    private fun generateStartGameActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageOk = appContext.getString(R.string.chat_action_sure)
        chatActions += ChatAction(
            messageOk,
            getOkActionForText(messageOk),
            R.drawable.selector_chat_action_accent
        )
        val messageSure = appContext.getString(R.string.chat_action_yes)
        chatActions += ChatAction(
            messageSure,
            getOkActionForText(messageSure),
            R.drawable.selector_chat_action_green
        )

        return chatActions
    }

    private fun getOkActionForText(text: String): (Int) -> Unit = {
        preferences.setIntroDialogShown(true)
        viewState.removeChatAction(it)
        viewState.showChatMessage(text, player)
        Single.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        router.newRootScreen(Constants.Screens.QUIZ, quiz.id)
                    }
                )
    }
}