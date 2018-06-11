package ru.kuchanov.scpquiz.mvp.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.db.FinishedLevels
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.mvp.view.GameView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class GamePresenter @Inject constructor(
    private var appDatabase: AppDatabase,
    private var router: Router
) : MvpPresenter<GameView>() {

    var quizId: Long by Delegates.notNull()

    lateinit var quiz: Quiz

    val enteredName = mutableListOf<Char>()

    init {
        Timber.d("constructor")
    }

    override fun onFirstViewAttach() {
        Timber.d("onFirstViewAttach")
        super.onFirstViewAttach()

        loadLevel()
    }

    fun loadLevel() {
        Timber.d("loadLevel")

        viewState.showProgress(true)

        val randomQuizesSingle = appDatabase.quizDao().getRandomQuizes(2)
                .flatMap { Flowable.fromIterable(it) }
                .map {
                    //todo create prefs for lang and use it
                    appDatabase.quizDao().getQuizTranslationsByQuizIdAndLang(it.id, "ru").first()
                }
                .limit(2)
                .toList()

        Single.zip(
            //todo create prefs for lang and use it
            Single.fromCallable {
                val quiz = appDatabase.quizDao().getQuizWithTranslationsAndPhrases(quizId, "ru")
                Timber.d("quiz:$quiz")
                quiz
            },
            randomQuizesSingle,
            BiFunction { quiz: Quiz, quizTranslations: List<QuizTranslation> -> Pair(quiz, quizTranslations) }
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        Timber.d("quiz:${it.first.scpNumber}\ntranslationTexts:${it.second.map { it.translation }}")

                        quiz = it.first

                        viewState.showProgress(false)
                        viewState.showLevel(quiz, it.second)
                    },
                    onError = {
                        Timber.e(it)
                        viewState.showProgress(false)
                        viewState.showError(it)
                    }
                )
    }

    fun onLevelCompleted() {
        //mark level as completed
        Single.fromCallable {
            appDatabase.finishedLevelsDao().insert(
                FinishedLevels(
                    quizId = quizId,
                    finished = true
                ))
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

        enteredName += char.toLowerCase()

        //check result
        quiz.quizTranslations?.get(0)?.let {
            if (enteredName.joinToString("").toLowerCase() == it.translation.toLowerCase()) {
                Timber.d("level completed!")
                //todo

                viewState.showLevelCompleted()
            } else {
                //todo?
            }
        }
    }

    fun onCharRemoved(char: Char) {
        enteredName.remove(char.toLowerCase())
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}