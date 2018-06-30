package ru.kuchanov.scpquiz.mvp.presenter.intro

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.IntroDialogView
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class IntroDialogPresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    private var appDatabase: AppDatabase
) : BasePresenter<IntroDialogView>(appContext, preferences, router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        //todo test
        viewState.showChatMessage(
            "test", User(
                name = appContext.getString(R.string.doctor_name),
                role = UserRole.DOCTOR
            ))

//        Flowable.interval(1, 2, TimeUnit.SECONDS)
        Flowable.intervalRange(
            0,
            2,
            1,
            2,
            TimeUnit.SECONDS
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        //todo correct user from DB
                        viewState.showChatMessage(
                            "test", User(
                                name = appContext.getString(R.string.doctor_name),
                                role = UserRole.DOCTOR
                            ))
                    },
                    onComplete = {
                        viewState.showChatActions(generateStartGameActions())
                    }
                )
    }

    private fun generateStartGameActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        chatActions += ChatAction(appContext.getString(R.string.chat_action_sure)) {
            //todo correct id from DB
            router.newRootScreen(Constants.Screens.QUIZ, 2L)
        }
        chatActions += ChatAction(appContext.getString(R.string.chat_action_yes)) {
            //todo correct id from DB
            router.newRootScreen(Constants.Screens.QUIZ, 2L)
        }

        return chatActions
    }
}