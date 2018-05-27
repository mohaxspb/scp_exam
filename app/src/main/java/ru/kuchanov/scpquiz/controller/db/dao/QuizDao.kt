package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.model.db.QuizTranslationPhrase


@Dao
interface QuizDao {

    @Query("SELECT COUNT(*) FROM Quiz")
    fun getCountFlowable(): Flowable<Long>

    @Query("SELECT COUNT(*) FROM Quiz")
    fun getCount(): Long

    @Query("SELECT * FROM Quiz")
    fun getAll(): Flowable<List<Quiz>>

    @Query("SELECT * FROM Quiz WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<Quiz>>

    @Query("SELECT * FROM Quiz WHERE id = :id")
    fun getById(id: Long): Quiz

    @Query("SELECT * FROM QuizTranslation WHERE quizId = :id")
    fun getQuizTranslationsByQuizId(id: Long): List<QuizTranslation>

    @Query("SELECT * FROM QuizTranslationPhrase WHERE quizTranslationId = :id")
    fun getQuizTranslationPhrasesByQuizTranslationId(id: Long): List<QuizTranslationPhrase>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<Quiz>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(quiz: Quiz): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuizTranslations(list: List<QuizTranslation>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuizTranslationPhrases(list: List<QuizTranslationPhrase>): List<Long>

    @Update
    fun update(quiz: Quiz): Int

    @Delete
    fun delete(quiz: Quiz): Int

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

        return insert(quiz)
    }

    @Transaction
    fun insertQuizesWithQuizTranslations(quizes: List<Quiz>) = quizes.map { insertQuizWithQuizTranslations(it) }

    @Transaction
    fun getQuizWithQuizTranslations(id: Long): Quiz {
        val quiz = getById(id)
        quiz.quizTranslations = getQuizTranslationsByQuizId(id)
        quiz.quizTranslations?.forEach { quizTranslation ->
            quizTranslation.quizTranslationPhrases = getQuizTranslationPhrasesByQuizTranslationId(quizTranslation.id)
        }
        return quiz
    }
}