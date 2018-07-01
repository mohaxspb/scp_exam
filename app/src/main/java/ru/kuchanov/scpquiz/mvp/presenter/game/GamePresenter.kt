package ru.kuchanov.scpquiz.mvp.presenter.game

import android.app.Application
import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.interactor.GameInteractor
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.QuizLevelInfo
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.GameView
import ru.kuchanov.scpquiz.ui.fragment.GameFragment
import ru.kuchanov.scpquiz.ui.view.KeyboardView
import ru.kuchanov.scpquiz.utils.BitmapUtils
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class GamePresenter @Inject constructor(
    override var appContext: Application,
    override var preferences: MyPreferenceManager,
    override var router: ScpRouter,
    private var gameInteractor: GameInteractor
) : BasePresenter<GameView>(appContext, preferences, router) {

    companion object {
        const val PERIODIC_MESSAGES_INITIAL_DELAY = 30L
        const val PERIODIC_MESSAGES_PERIOD = 30L
    }

    private var currentLang: String = preferences.getLang()

    private var isLevelShown: Boolean = false

    var quizId: Long by Delegates.notNull()

    lateinit var quizLevelInfo: QuizLevelInfo

    private val enteredName = mutableListOf<Char>()

    private val enteredNumber = mutableListOf<Char>()

    private var levelDataDisposable: Disposable? = null

    private var periodicMessagesDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        loadLevel()
    }

    private fun sendPeriodicMessages() {
        periodicMessagesDisposable = Flowable.interval(
            PERIODIC_MESSAGES_INITIAL_DELAY,
            PERIODIC_MESSAGES_PERIOD,
            TimeUnit.SECONDS
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    val phrases = quizLevelInfo.quiz.quizTranslations?.first()?.quizTranslationPhrases
                    if (phrases?.isNotEmpty() == true) {
                        phrases[Random().nextInt(phrases.size)].translation.let {
                            viewState.showChatMessage(it, quizLevelInfo.doctor)
                        }
                    }
                }
    }

    private fun loadLevel() {
        Timber.d("loadLevelInfo")

        viewState.showProgress(true)

        levelDataDisposable = gameInteractor
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
                                        with(viewState) {
                                            showToolbar(true)
                                            //todo param for number
                                            showLevelNumber(-1)

                                            val chars = quizLevelInfo.quiz.quizTranslations?.let {
                                                it[0].translation.toMutableList()
                                            } ?: throw IllegalStateException("translations is null")
                                            val availableChars = quizLevelInfo.randomTranslations
                                                    .joinToString(separator = "") { it.translation }
                                                    .toList()
                                            setKeyboardChars(
                                                KeyboardView.fillCharsList(
                                                    chars,
                                                    availableChars
                                                ).apply { shuffle() }
                                            )
                                            animateKeyboard()
                                        }

                                        sendPeriodicMessages()
                                    }
                                    !scpNumberFilled && scpNameFilled -> {
                                        with(viewState) {
                                            showToolbar(true)
                                            showLevelNumber(-1)
                                            val scpNumberChars = quizLevelInfo.quiz.scpNumber.toMutableList()
                                            setKeyboardChars(
                                                KeyboardView.fillCharsList(
                                                    scpNumberChars,
                                                    Constants.DIGITS_CHAR_LIST
                                                ).shuffled()
                                            )
                                            showChatMessage(
                                                appContext.getString(
                                                    R.string.message_enter_number_description,
                                                    Constants.COINS_FOR_NUMBER
                                                ),
                                                quizLevelInfo.doctor
                                            )
                                            showKeyboard(true)
                                            showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
                                        }

                                        sendPeriodicMessages()
                                    }
                                    scpNumberFilled && scpNameFilled -> {
                                        with(viewState) {
                                            showKeyboard(false)
                                            showToolbar(false)
                                            setBackgroundDark(true)
                                            showChatMessage(
                                                quizLevelInfo.quiz.quizTranslations!!.first().description,
                                                quizLevelInfo.player
                                            )
                                            showChatMessage(
                                                appContext.getString(
                                                    R.string.message_level_comleted,
                                                    quizLevelInfo.player.name),
                                                quizLevelInfo.doctor
                                            )
                                            showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
                                            showNumber(quizLevelInfo.quiz.scpNumber.toList())
                                            showChatActions(generateLevelCompletedActions())
                                        }
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

    fun onLevelsClicked() = router.navigateTo(Constants.Screens.QUIZ_LIST)

    fun onCoinsClicked() {
        //todo
        Timber.d("coins button clicked!")
    }

    fun onHamburgerMenuClicked() {
        Timber.d("hamburgerButton button clicked!")
        viewState.onNeedToOpenSettings()
    }

    fun openSettings(bitmap: Bitmap) {
        Completable.fromAction {
            BitmapUtils.persistImage(
                appContext,
                bitmap,
                Constants.SETTINGS_BACKGROUND_FILE_NAME)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = { router.navigateTo(Constants.Screens.SETTINGS) }
                )
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
        if (quizLevelInfo.quiz.scpNumber.toLowerCase() == enteredNumber.joinToString("").toLowerCase()) {
            Timber.d("number is correct!")

            quizLevelInfo.finishedLevel.scpNumberFilled = true
            onLevelCompleted()

            with(viewState) {
                showKeyboard(false)
                showToolbar(false)
                setBackgroundDark(true)

                showChatMessage(
                    quizLevelInfo.quiz.quizTranslations!!.first().description,
                    quizLevelInfo.player
                )
                showChatMessage(
                    appContext.getString(R.string.message_correct_give_coins, Constants.COINS_FOR_NUMBER),
                    quizLevelInfo.doctor
                )
                showChatMessage(
                    appContext.getString(R.string.message_level_comleted, quizLevelInfo.player.name),
                    quizLevelInfo.doctor
                )

                showChatActions(generateLevelCompletedActions())
            }

            periodicMessagesDisposable?.dispose()
        } else {
            Timber.d("number is not correct")
        }
    }

    private fun checkEnteredScpName() {
        quizLevelInfo.quiz.quizTranslations?.first()?.let { quizTranslation ->
            if (enteredName.joinToString("").toLowerCase() == quizTranslation.translation.toLowerCase()) {
                Timber.d("name is correct!")
                with(viewState) {
                    quizLevelInfo.finishedLevel.scpNameFilled = true
                    onLevelCompleted()

                    showChatMessage(
                        appContext.getString(R.string.message_correct_give_coins, Constants.COINS_FOR_NAME),
                        quizLevelInfo.doctor
                    )
                    showChatMessage(
                        appContext.getString(R.string.message_suggest_scp_number),
                        quizLevelInfo.doctor
                    )


                    showChatActions(generateNameEnteredChatActions())
                    showKeyboard(false)
                }
            } else {
                Timber.d("name is not correct")
            }
        }
    }

    private fun generateLevelCompletedActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()
        if (quizLevelInfo.nextQuizId != GameFragment.NO_NEXT_QUIZ_ID) {
            val nextLevelAction = ChatAction(
                appContext.getString(R.string.chat_action_next_level),
                {
                    router.replaceScreen(Constants.Screens.QUIZ, quizLevelInfo.nextQuizId)
                },
                R.drawable.selector_chat_action_green
            )
            chatActions += nextLevelAction
        }

        val enterNumberAction = ChatAction(
            appContext.getString(R.string.chat_action_levels_list),
            {
                router.backTo(Constants.Screens.QUIZ_LIST)
            },
            R.drawable.selector_chat_action_accent
        )
        chatActions += enterNumberAction
        return chatActions
    }

    private fun generateNameEnteredChatActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()
        if (quizLevelInfo.nextQuizId != GameFragment.NO_NEXT_QUIZ_ID) {
            val nextLevelAction = ChatAction(
                appContext.getString(R.string.chat_action_next_level),
                {
                    router.replaceScreen(Constants.Screens.QUIZ, quizLevelInfo.nextQuizId)
                },
                R.drawable.selector_chat_action_accent
            )
            chatActions += nextLevelAction
        }
        val message = appContext.getString(R.string.chat_action_enter_number)
        val enterNumberAction = ChatAction(
            message,
            {
                val availableChars = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').shuffled()
                val scpNumberChars = quizLevelInfo.quiz.scpNumber.toCharArray().toMutableList()
                with(viewState) {
                    setKeyboardChars(KeyboardView.fillCharsList(scpNumberChars, availableChars))
                    showKeyboard(true)
                    showChatMessage(message, quizLevelInfo.player)
                    removeChatAction(it)
                }
            },
            R.drawable.selector_chat_action_green
        )
        chatActions += enterNumberAction
        return chatActions
    }

    private fun onLevelCompleted() {
        //mark level as completed
        gameInteractor.updateFinishedLevel(
            quizId,
            quizLevelInfo.finishedLevel.scpNameFilled,
            quizLevelInfo.finishedLevel.scpNumberFilled
        )
                .flatMap { gameInteractor.getNumberOfFinishedLevels() }
                .subscribeBy(
                    onSuccess = {
                        Timber.d("updated!")
                        if (it == Constants.FINISHED_LEVEL_BEFORE_ASK_RATE_APP) {
                            viewState.askForRateApp()
                        }
                    },
                    onError = {
                        Timber.e(it)
                        viewState.showError(it)
                    }
                )
    }

    fun checkLang() {
        val langInPrefs = preferences.getLang()
        if (currentLang != langInPrefs) {
            isLevelShown = false
            viewState.clearChatMessages()
            loadLevel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (periodicMessagesDisposable?.isDisposed == false) {
            periodicMessagesDisposable?.dispose()
        }
        levelDataDisposable?.dispose()
    }
}