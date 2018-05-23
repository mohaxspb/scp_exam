package ru.kuchanov.scpquiz.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation


@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz")
    abstract fun getAll(): Flowable<List<Quiz>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    abstract fun getByIdWithUpdates(id: Long): Flowable<List<Quiz>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    abstract fun getById(id: Long): Quiz

    @Query("SELECT * FROM quiztranslation WHERE quizId = :id")
    abstract fun getQuizTranslationsByQuizId(id: Long): List<QuizTranslation>

    @Query("SELECT * FROM quiz WHERE id = :id")
    abstract fun getByIdOrErrorOnce(id: Long): Single<Quiz>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(quiz: Quiz): Long

    @Insert
    abstract fun insertQuizTranslations(list: List<QuizTranslation>): List<Long>

    @Update
    abstract fun update(quiz: Quiz): Int

    @Delete
    abstract fun delete(quiz: Quiz): Int

    @Transaction
    fun insertQuizWithQuizTranslations(quiz: Quiz): Long {
        quiz.quizTranslations?.let {
            it.forEach {
                it.quizId = quiz.id
            }
            insertQuizTranslations(it)
        }

        return insert(quiz)
    }

    @Transaction
    fun insertQuizesWithQuizTranslations(quizes: List<Quiz>) = quizes.map { insertQuizWithQuizTranslations(it) }

    @Transaction
    fun getQuizWithQuizTranslations(id: Long): Quiz {
        val quiz = getById(id)
        val quizTranslations = getQuizTranslationsByQuizId(id)
        quiz.quizTranslations = quizTranslations
        return quiz
    }
}