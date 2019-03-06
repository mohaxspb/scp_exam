package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function6
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.model.db.*
import ru.kuchanov.scpquiz.model.ui.QuizLevelInfo
import timber.log.Timber
import javax.inject.Inject

class GameInteractor @Inject constructor(
        private val appDatabase: AppDatabase,
        private val preferenceManager: MyPreferenceManager
) {

    fun getLevelInfo(quizId: Long): Flowable<QuizLevelInfo> = Flowable.combineLatest(
            getQuiz(quizId).doOnNext { Timber.d("getQuiz") },
            getRandomTranslations().doOnNext { Timber.d(" getRandomTranslations()") },
            getPlayer().doOnNext { Timber.d("getPlayer()") },
            getDoctor().doOnNext { Timber.d("getDoctor()") },
            getFinishedLevel(quizId).doOnNext { Timber.d("getFinishedLevel(quizId)") },
            getNextQuizIdAndFinishedLevel(quizId).doOnNext { Timber.d("getNextQuizIdAndFinishedLevel") },
            Function6 { quiz: Quiz,
                        randomTranslations: List<QuizTranslation>,
                        player: User,
                        doctor: User,
                        finishedLevel: FinishedLevel,
                        nextQuizIdAndFinishedLevel: Pair<Long?, FinishedLevel?> ->
                QuizLevelInfo(
                        quiz = quiz,
                        randomTranslations = randomTranslations,
                        player = player,
                        doctor = doctor,
                        finishedLevel = finishedLevel,
                        nextQuizIdAndFinishedLevel = nextQuizIdAndFinishedLevel
                )
            }
    )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun updateFinishedLevel(
            quizId: Long,
            scpNameFilled: Boolean? = null,
            scpNumberFilled: Boolean? = null,
            nameRedundantCharsRemoved: Boolean? = null,
            numberRedundantCharsRemoved: Boolean? = null,
            isLevelAvailable: Boolean? = null
    ): Single<Long> = Single.fromCallable {
        with(appDatabase.finishedLevelsDao().getByIdOrErrorOnce(quizId).blockingGet()) {
            scpNameFilled?.let { this.scpNameFilled = scpNameFilled }
            scpNumberFilled?.let { this.scpNumberFilled = scpNumberFilled }
            nameRedundantCharsRemoved?.let { this.nameRedundantCharsRemoved = nameRedundantCharsRemoved }
            numberRedundantCharsRemoved?.let { this.numberRedundantCharsRemoved = numberRedundantCharsRemoved }
            this.isLevelAvailable = isLevelAvailable ?: this.scpNameFilled
                    || this.scpNumberFilled
                    || this.nameRedundantCharsRemoved
                    || this.numberRedundantCharsRemoved
            appDatabase.finishedLevelsDao().insert(this)
        }
    }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun getNumberOfPartiallyAndFullyFinishedLevels(): Single<Pair<Long, Long>> = Single
            .zip(
                    Single.fromCallable { appDatabase.finishedLevelsDao().getCountOfPartiallyFinishedLevels() },
                    Single.fromCallable { appDatabase.finishedLevelsDao().getCountOfFullyFinishedLevels() },
                    BiFunction { partiallyFinished: Long, fullyFinished: Long -> Pair(partiallyFinished, fullyFinished) }
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun getRandomTranslations() = appDatabase.quizDao().getRandomQuizTranslationsForThisLang(2, preferenceManager.getLang())
            .toFlowable()

    private fun getQuiz(quizId: Long) = Single.fromCallable {
        val quiz = appDatabase.quizDao().getQuizWithTranslationsAndPhrases(quizId, preferenceManager.getLang())
        Timber.d("quiz:${quiz.quizTranslations?.first()?.translation}")
        quiz
    }
            .toFlowable()

    private fun getPlayer() = appDatabase.userDao().getByRoleWithUpdates(UserRole.PLAYER).map { it.first() }

    private fun getDoctor() = appDatabase.userDao().getOneByRole(UserRole.DOCTOR).toFlowable()

    private fun getFinishedLevel(quizId: Long) = appDatabase.finishedLevelsDao()
            .getByIdWithUpdates(quizId)
            .map { it.first() }

    private fun getNextQuizIdAndFinishedLevel(quizId: Long) = appDatabase.quizDao()
            .getNextQuizId(quizId)
            .flatMap<Pair<Long?, FinishedLevel?>> { nextQuizId ->
                appDatabase.finishedLevelsDao()
                        .getByIdOrErrorOnce(nextQuizId)
                        .map { Pair(nextQuizId, it) }
            }
            .onErrorReturn { Pair(null, null) }
            .toFlowable()

    fun increaseScore(score: Int): Completable = Completable.fromAction {
        with(appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet()) {
            this.score += score
            appDatabase.userDao().update(this).toLong()
        }
    }
}