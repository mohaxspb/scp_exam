package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.model.db.QuizTranslationPhrase


@Dao
interface QuizDao {

    @Query("SELECT COUNT(*) FROM Quiz")
    fun getCountFlowable(): Flowable<Long>

    @Query("SELECT COUNT(*) FROM Quiz")
    fun getCount(): Long

    /**
     * returns all quizes, sorted by ID ASC
     */
    @Query("SELECT * FROM Quiz ORDER BY id ASC")
    fun getAll(): Flowable<List<Quiz>>

    @Query("SELECT * FROM Quiz ORDER BY id ASC")
    fun getAllList(): List<Quiz>

    @Query("SELECT * FROM Quiz ORDER BY RANDOM() LIMIT :count")
    fun getRandomQuizes(count: Int = 1): Flowable<List<Quiz>>

    @Query("SELECT * FROM Quiz WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<Quiz>>

    @Query("SELECT * FROM Quiz WHERE id = :id")
    fun getById(id: Long): Quiz

    @Query("SELECT * FROM QuizTranslation WHERE quizId = :id")
    fun getQuizTranslationsByQuizId(id: Long): List<QuizTranslation>

    @Query("SELECT * FROM QuizTranslation WHERE quizId = :id AND langCode = :lang")
    fun getQuizTranslationsByQuizIdAndLang(id: Long, lang: String): List<QuizTranslation>

    @Query("SELECT * FROM QuizTranslationPhrase WHERE quizTranslationId = :id")
    fun getQuizTranslationPhrasesByQuizTranslationId(id: Long): List<QuizTranslationPhrase>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<Quiz>

    @Query("SELECT * FROM quiz ORDER BY id ASC LIMIT 1")
    fun getFirst(): Single<Quiz>

    @Query("SELECT * FROM FinishedLevel WHERE quizId = :quizId")
    fun getFinishedLevelById(quizId: Long): FinishedLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(quiz: Quiz): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuizTranslations(list: List<QuizTranslation>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuizTranslationPhrases(list: List<QuizTranslationPhrase>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFinishedLevel(finishedLevel: FinishedLevel): Long

    @Update
    fun update(quiz: Quiz): Int

    @Delete
    fun delete(quiz: Quiz): Int

    @Query("DELETE FROM Quiz")
    fun deleteAll()

    //finished levels

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(finishedLevel: FinishedLevel): Long

    @Query("SELECT * FROM FinishedLevel WHERE quizId = :quizId")
    fun getByQuizId(quizId: Long): FinishedLevel?

    @Transaction
    fun insertQuizWithQuizTranslations(quiz: Quiz): Long {
        quiz.quizTranslations?.let { quizTranslations ->
            quizTranslations.forEach { quizTranslation ->
                quizTranslation.quizId = quiz.id
                quizTranslation.quizTranslationPhrases?.let { quizTranslationPhrases ->
                    quizTranslationPhrases.forEach { quizTranslationPhrase ->
                        quizTranslationPhrase.quizTranslationId = quizTranslation.id
                    }
                    insertQuizTranslationPhrases(quizTranslationPhrases)
                }
            }
            insertQuizTranslations(quizTranslations)
        }
        if (getFinishedLevelById(quiz.id) == null) {
            insertFinishedLevel(FinishedLevel(quiz.id))
        }
        return insert(quiz)
    }

    @Transaction
    fun insertQuizesWithQuizTranslationsWithFinishedLevels(quizes: List<Quiz>) {
        insertQuizesWithQuizTranslations(quizes)
        quizes.forEach { quiz ->
            if (getByQuizId(quiz.id) == null) {
                insert(
                        FinishedLevel(
                                quizId = quiz.id,
                                scpNameFilled = false,
                                scpNumberFilled = false,
                                nameRedundantCharsRemoved = false,
                                numberRedundantCharsRemoved = false,
                                isLevelAvailable = false
                        )
                )
            }
        }
    }

    @Transaction
    fun insertQuizesWithQuizTranslations(quizes: List<Quiz>) = quizes.map { insertQuizWithQuizTranslations(it) }

    @Transaction
    fun getQuizWithTranslationsAndPhrases(id: Long): Quiz {
        val quiz = getById(id)
        quiz.quizTranslations = getQuizTranslationsByQuizId(id)
        quiz.quizTranslations?.forEach { quizTranslation ->
            quizTranslation.quizTranslationPhrases = getQuizTranslationPhrasesByQuizTranslationId(quizTranslation.id)
        }
        return quiz
    }

    @Transaction
    fun getQuizWithTranslationsAndPhrases(id: Long, lang: String): Quiz {
        val quiz = getById(id)
        quiz.quizTranslations = getQuizTranslationsByQuizIdAndLang(id, lang)
        quiz.quizTranslations?.forEach { quizTranslation ->
            quizTranslation.quizTranslationPhrases = getQuizTranslationPhrasesByQuizTranslationId(quizTranslation.id)
        }
        return quiz
    }

    @Query("SELECT id FROM quiz WHERE id > :quizId ORDER BY id ASC LIMIT 1")
    fun getNextQuizId(quizId: Long): Single<Long>
}