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
import ru.kuchanov.scpquiz.ui.view.KeyboardView
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

    private val enteredNumber = mutableListOf<Char>()

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
                    onNext = { levelInfo ->
                        Timber.d("quiz:${levelInfo.quiz.scpNumber}\ntranslationTexts:${levelInfo.randomTranslations.map { it.translation }}")

                        quizLevelInfo = levelInfo

                        viewState.showProgress(false)
                        if (!isLevelShown) {
                            isLevelShown = true
                            quizLevelInfo.finishedLevel.apply {
                                viewState.showImage(quizLevelInfo.quiz)
                                when {
                                    !scpNumberFilled && !scpNameFilled -> {
                                        viewState.showToolbar(true)
                                        //todo param for number
                                        viewState.showLevelNumber(-1)

                                        val chars = quizLevelInfo.quiz.quizTranslations?.let {
                                            it[0].translation.toMutableList()
                                        } ?: throw IllegalStateException("translations is null")
                                        val availableChars = quizLevelInfo.randomTranslations
                                                .joinToString(separator = "") { it.translation }
                                                .toList()
                                        viewState.setKeyboardChars(
                                            KeyboardView.fillCharsList(
                                                chars,
                                                availableChars
                                            ).apply { shuffle() }
                                        )
                                        viewState.animateKeyboard()
                                    }
                                    !scpNumberFilled && scpNameFilled -> {
                                        //show number keyboard
                                        val availableChars = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').shuffled()
                                        val scpNumberChars = quizLevelInfo.quiz.scpNumber.toMutableList()
                                        viewState.setKeyboardChars(KeyboardView.fillCharsList(scpNumberChars, availableChars))
                                        viewState.showKeyboard(true)
                                        viewState.showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
                                    }
                                    scpNumberFilled && scpNameFilled -> {
                                        viewState.showKeyboard(false)
                                        viewState.showToolbar(false)
                                        viewState.setBackgroundDark(true)
                                        viewState.showChatMessage(
                                            quizLevelInfo.quiz.quizTranslations!!.first().description,
                                            quizLevelInfo.player
                                        )
                                        viewState.showChatMessage(
                                            appContext.getString(R.string.message_level_comleted, quizLevelInfo.player.name),
                                            quizLevelInfo.doctor
                                        )
                                        viewState.showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
                                        viewState.showNumber(quizLevelInfo.quiz.scpNumber.toList())
                                        //todo next level/list action
                                    }
                                }
                            }
                        }

                        viewState.showCoins(quizLevelInfo.player.score)
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

        if (!quizLevelInfo.finishedLevel.scpNameFilled) {
            enteredName += char.toLowerCase()
            //check result
            checkEnteredScpName()
        } else {
            enteredNumber += char.toLowerCase()
            checkEnteredScpNumber()
        }
    }

    fun onCharRemoved(char: Char, indexOfChild: Int) {
        Timber.d("onCharRemoved: $char, $indexOfChild")
        if (!quizLevelInfo.finishedLevel.scpNameFilled) {
            enteredName.removeAt(indexOfChild)
        } else {
            enteredNumber.removeAt(indexOfChild)
        }
    }

    private fun checkEnteredScpNumber() {
        if (quizLevelInfo.quiz.scpNumber == enteredNumber.joinToString("")) {
            Timber.d("number is correct!")

            quizLevelInfo.finishedLevel.scpNumberFilled = true
            onLevelCompleted()

            viewState.showChatMessage(
                appContext.getString(R.string.message_correct_give_coins, Constants.COINS_FOR_NUMBER),
                quizLevelInfo.doctor
            )

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

            val enterNumberAction = ChatAction(
                appContext.getString(R.string.chat_action_levels_list),
                {
                    router.backTo(Constants.Screens.QUIZ_LIST)
                }
            )
            chatActions += enterNumberAction
            viewState.showChatActions(chatActions, 0)
            viewState.showKeyboard(false)

            //todo show completed state
        } else {
            Timber.d("number is not correct")
        }
    }

    private fun checkEnteredScpName() {
        quizLevelInfo.quiz.quizTranslations?.first()?.let { quizTranslation ->
            if (enteredName.joinToString("").toLowerCase() == quizTranslation.translation.toLowerCase()) {
                Timber.d("level completed!")

                quizLevelInfo.finishedLevel.scpNameFilled = true
                onLevelCompleted()

                viewState.showChatMessage(
                    appContext.getString(R.string.message_correct_give_coins, Constants.COINS_FOR_NAME),
                    quizLevelInfo.doctor
                )
                viewState.showChatMessage(
                    appContext.getString(R.string.message_suggest_scp_number),
                    quizLevelInfo.doctor
                )

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
                        val availableChars = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').shuffled()
                        val scpNumberChars = quizLevelInfo.quiz.scpNumber.toCharArray().toMutableList()
                        viewState.setKeyboardChars(KeyboardView.fillCharsList(scpNumberChars, availableChars))
                        viewState.showKeyboard(true)
                        viewState.showChatMessage(message, quizLevelInfo.player)
                        viewState.removeChatAction(it)
                    }
                )
                chatActions += enterNumberAction
                viewState.showChatActions(chatActions, 0)
                viewState.showKeyboard(false)
            } else {
                Timber.d("name is not correct")
            }
        }
    }

    private fun onLevelCompleted() {
        //mark level as completed
        gameInteractor.updateFinishedLevel(
            quizId,
            quizLevelInfo.finishedLevel.scpNameFilled,
            quizLevelInfo.finishedLevel.scpNumberFilled
        )
                .subscribeBy(
                    onSuccess = {
                        Timber.d("updated!")
                    },
                    onError = {
                        Timber.e(it)
                        viewState.showError(it)
                    }
                )
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}