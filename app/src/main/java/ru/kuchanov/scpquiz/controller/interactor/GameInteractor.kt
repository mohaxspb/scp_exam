package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function6
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.model.db.*
import ru.kuchanov.scpquiz.model.ui.QuizLevelInfo
import ru.kuchanov.scpquiz.ui.fragment.GameFragment
import timber.log.Timber
import javax.inject.Inject

class GameInteractor @Inject constructor(
    private val appDatabase: AppDatabase,
    private val preferenceManager: MyPreferenceManager
) {

    fun getLevelInfo(quizId: Long): Flowable<QuizLevelInfo> = Flowable.combineLatest(
        getQuiz(quizId),
        getRandomTranslations(),
        getPlayer(),
        getDoctor(),
        getFinishedLevel(quizId),
        getNextQuizId(quizId),
        Function6 { quiz: Quiz,
            randomTranslations: List<QuizTranslation>,
            player: User,
            doctor: User,
            finishedLevel: FinishedLevel,
            nextQuizId: Long ->
            QuizLevelInfo(
                quiz = quiz,
                randomTranslations = randomTranslations,
                player = player,
                doctor = doctor,
                finishedLevel = finishedLevel,
                nextQuizId = nextQuizId
            )
        }
    )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun updateFinishedLevel(
        quizId: Long,
        isNameFilled: Boolean,
        isNumberFilled: Boolean
    ): Single<Long> = Single.fromCallable {
        appDatabase.finishedLevelsDao().insert(
            FinishedLevel(
                quizId = quizId,
                scpNameFilled = isNameFilled,
                scpNumberFilled = isNumberFilled
            ))
    }
            .flatMap {
                val user = appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet()
                user.score += when {
                    isNameFilled && isNumberFilled -> Constants.COINS_FOR_NUMBER
                    isNameFilled && !isNumberFilled -> Constants.COINS_FOR_NAME
                    else -> 0
                }
                Single.fromCallable { appDatabase.userDao().update(user).toLong() }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun getNumberOfFinishedLevels(): Single<Long> = Single.fromCallable { appDatabase.finishedLevelsDao().getCountOfPartiallyFinishedLevels() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun getRandomTranslations() = appDatabase.quizDao().getRandomQuizes(2)
            .flatMap { Flowable.fromIterable(it) }
            .map {
                appDatabase.quizDao().getQuizTranslationsByQuizIdAndLang(it.id, preferenceManager.getLang()).first()
            }
            .limit(2)
            .toList()
            .toFlowable()

    private fun getQuiz(quizId: Long) = Single.fromCallable {
        val quiz = appDatabase.quizDao().getQuizWithTranslationsAndPhrases(quizId, preferenceManager.getLang())
        Timber.d("quiz:$quiz")
        quiz
    }
            .toFlowable()

    private fun getPlayer() = appDatabase.userDao().getByRoleWithUpdates(UserRole.PLAYER).map { it.first() }

    private fun getDoctor() = appDatabase.userDao().getOneByRole(UserRole.DOCTOR).toFlowable()

    private fun getFinishedLevel(quizId: Long) = appDatabase.finishedLevelsDao()
            .getByIdWithUpdates(quizId)
            .map { it.first() }

    private fun getNextQuizId(quizId: Long) = appDatabase.quizDao()
            .getNextQuizId(quizId)
            .onErrorReturn { GameFragment.NO_NEXT_QUIZ_ID }
            .toFlowable()
}