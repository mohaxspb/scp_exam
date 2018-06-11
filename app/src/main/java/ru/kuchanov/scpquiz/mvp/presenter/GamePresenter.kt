package ru.kuchanov.scpquiz.mvp.presenter

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.rxkotlin.subscribeBy
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.interactor.GameInteractor
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.QuizLevelInfo
import ru.kuchanov.scpquiz.mvp.view.GameView
import ru.kuchanov.scpquiz.ui.fragment.GameFragment
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class GamePresenter @Inject constructor(
    private var appContext: Application,
    private var gameInteractor: GameInteractor,
    private var router: Router
) : MvpPresenter<GameView>() {

    private var isLevelShown: Boolean = false

    var quizId: Long by Delegates.notNull()

    lateinit var quizLevelInfo: QuizLevelInfo

    private val enteredName = mutableListOf<Char>()

    var isScpNameCompleted = false

    private var isScpNumberCompleted = false

    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        Timber.d("onFirstViewAttach")
        super.onFirstViewAttach()

        loadLevel()
    }

    private fun loadLevel() {
        Timber.d("loadLevelInfo")

        viewState.showProgress(true)

        gameInteractor
                .getLevelInfo(quizId)
                .subscribeBy(
                    onNext = {
                        Timber.d("quiz:${it.quiz.scpNumber}\ntranslationTexts:${it.randomTranslations.map { it.translation }}")

                        quizLevelInfo = it

                        viewState.showProgress(false)
                        if (!isLevelShown) {
                            isLevelShown = true
                            viewState.showLevel(it.quiz, it.randomTranslations)
                        }
                    },
                    onError = {
                        Timber.e(it)
                        viewState.showProgress(false)
                        viewState.showError(it)
                    }
                )
    }

    fun onLevelsClicked() = router.backTo(Constants.Screens.QUIZ_LIST)

    fun onCoinsClicked() {
        //todo
        Timber.d("coins button clicked!")
    }

    fun onHamburgerMenuClicked() {
        //todo
        Timber.d("hamburgerButton button clicked!")

        val randomMessage = if (Random().nextBoolean()) {
            "kjdhfdskjfh skjdh ksjh ksjdskjhksjhkjshkjshksjhf ksjhf kjshskjhskjfh"
        } else {
            "test"
        }

        viewState.showChatMessage(randomMessage, quizLevelInfo.doctor)
    }

    fun onCharClicked(char: Char) {
        Timber.d("char pressed: $char")

        if (!isScpNumberCompleted) {
            enteredName += char.toLowerCase()
            //check result
            checkEnteredScpName()
        } else {
            //todo check if number is correct
        }
    }

    private fun checkEnteredScpName() {
        quizLevelInfo.quiz.quizTranslations?.first()?.let {
            //            if (enteredName.joinToString("").toLowerCase() == it.translation.toLowerCase()) {
            //fixme test
            if (enteredName.size > 0) {
                Timber.d("level completed0!")

                isScpNameCompleted = true
                onLevelCompleted()

                //todo show state for different cases
//                viewState.showLevelCompleted()

                viewState.showChatMessage(appContext.getString(R.string.message_suggest_scp_number), quizLevelInfo.doctor)

                val chatActions = mutableListOf<ChatAction>()

                if (quizLevelInfo.nextQuizId != GameFragment.NO_NEXT_QUIZ_ID) {
                    val nextLevelAction = ChatAction(
                        appContext.getString(R.string.chat_action_next_level),
                        {
                            router.replaceScreen(Constants.Screens.QUIZ, quizLevelInfo.nextQuizId)
                        }
                    )
                    chatActions += nextLevelAction
                }
                val message = appContext.getString(R.string.chat_action_enter_number)
                val enterNumberAction = ChatAction(
                    message,
                    {
                        //todo need to pass correct numbers, as it can have duplicated ones
                        viewState.setKeyboardChars(listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'))
                        viewState.showKeyboard(true)
                        viewState.showChatMessage(message, quizLevelInfo.player)
                        viewState.removeChatAction(it)
                    }
                )
                chatActions += enterNumberAction
                viewState.showChatActions(chatActions, 0)
                viewState.showKeyboard(false)
            } else {
                Timber.d("check entered: ${enteredName.joinToString("").toLowerCase()}")
                Timber.d("check translation: ${it.translation.toLowerCase()}")
                //todo?
            }
        }
    }

    private fun onLevelCompleted() {
        //mark level as completed
        gameInteractor.updateFinishedLevel(
            quizId,
            isScpNameCompleted,
            isScpNumberCompleted
        )
                .subscribeBy(
                    onSuccess = {
                        Timber.d("updated!")
                    },
                    onError = {
                        Timber.e(it)
                        /*todo*/
                    }
                )
    }

    fun onCharRemoved(char: Char, indexOfChild: Int) {
        enteredName.removeAt(indexOfChild)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}