package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.db.*
import ru.kuchanov.scpquiz.model.ui.QuizLevelInfo
import timber.log.Timber
import javax.inject.Inject

class GameInteractor @Inject constructor(
    private var appDatabase: AppDatabase
) {

    fun getLevelInfo(quizId: Long): Flowable<QuizLevelInfo> = Flowable.combineLatest(
        getQuiz(quizId),
        getRandomTranslations(),
        getPlayer(),
        getDoctor(),
        getFinishedLevel(quizId),
        Function5 { quiz: Quiz,
            randomTranslations: List<QuizTranslation>,
            player: User,
            doctor: User,
            finishedLevel: FinishedLevel ->
            QuizLevelInfo(
                quiz = quiz,
                randomTranslations = randomTranslations,
                player = player,
                doctor = doctor,
                finishedLevel = finishedLevel
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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


    private fun getRandomTranslations() = appDatabase.quizDao().getRandomQuizes(2)
            .flatMap { Flowable.fromIterable(it) }
            .map {
                //todo create prefs for lang and use it
                appDatabase.quizDao().getQuizTranslationsByQuizIdAndLang(it.id, "ru").first()
            }
            .limit(2)
            .toList()
            .toFlowable()

    private fun getQuiz(quizId: Long) = Single.fromCallable {
        //todo create prefs for lang and use it
        val quiz = appDatabase.quizDao().getQuizWithTranslationsAndPhrases(quizId, "ru")
        Timber.d("quiz:$quiz")
        quiz
    }
            .toFlowable()

    private fun getPlayer() = appDatabase.userDao().getByRoleWithUpdates(UserRole.PLAYER).map { it.first() }

    private fun getDoctor() = appDatabase.userDao().getOneByRole(UserRole.DOCTOR).toFlowable()

    private fun getFinishedLevel(quizId: Long) = appDatabase.finishedLevelsDao()
            .getByIdWithUpdates(quizId)
            .map { it.first() }
}