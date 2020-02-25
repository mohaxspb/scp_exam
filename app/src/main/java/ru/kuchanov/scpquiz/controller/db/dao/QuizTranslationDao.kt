package ru.kuchanov.scpquiz.controller.db.dao

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.QuizTranslation


@Dao
interface QuizTranslationDao {

    @Query("SELECT * FROM quizTranslation")
    fun getAll(): Flowable<List<QuizTranslation>>

    @Query("SELECT * FROM quizTranslation WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<QuizTranslation>>

    @Query("SELECT * FROM quizTranslation WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<QuizTranslation>

    @Query("SELECT langCode FROM quizTranslation GROUP BY langCode")
    fun getAllLangsSignle(): Single<List<String>>

    @Query("SELECT langCode FROM quizTranslation GROUP BY langCode")
    fun getAllLangs(): List<String>

    @Insert
    fun insert(quiz: QuizTranslation): Long

    @Update
    fun update(quiz: QuizTranslation): Int

    @Delete
    fun delete(quiz: QuizTranslation): Int
}