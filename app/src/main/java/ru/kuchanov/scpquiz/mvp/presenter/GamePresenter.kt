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
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class GamePresenter @Inject constructor(
    private var appContext: Application,
    private var gameInteractor: GameInteractor,
    private var router: Router
) : MvpPresenter<GameView>() {

    var quizId: Long by Delegates.notNull()

//    var nextQuizId: Long by Delegates.notNull()

    lateinit var quizLevelInfo: QuizLevelInfo

    private val enteredName = mutableListOf<Char>()

    private var isScpNameCompleted = false

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
                        viewState.showLevel(it.quiz, it.randomTranslations)
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
        quizLevelInfo.quiz.quizTranslations?.get(0)?.let {
            if (enteredName.joinToString("").toLowerCase() == it.translation.toLowerCase()) {
                Timber.d("level completed!")

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
                val enterNumberAction = ChatAction(
                    appContext.getString(R.string.chat_action_enter_number),
                    {
                        viewState.showKeyboard(true)
                        //todo fill keyboard with digits
                    }
                )
                chatActions += enterNumberAction
                viewState.showChatActions(chatActions)
                viewState.showKeyboard(false)
            } else {
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

    fun onCharRemoved(char: Char) {
        enteredName.remove(char.toLowerCase())
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}