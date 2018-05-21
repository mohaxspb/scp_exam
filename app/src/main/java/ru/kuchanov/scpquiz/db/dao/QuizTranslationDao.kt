package ru.kuchanov.scpquiz.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.QuizTranslation


@Dao
interface QuizTranslationDao {

    @Query("SELECT * FROM quiztranslation")
    fun getAll(): Flowable<List<QuizTranslation>>

    @Query("SELECT * FROM quiztranslation WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<QuizTranslation>>

    @Query("SELECT * FROM quiztranslation WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<QuizTranslation>

    @Insert
    fun insert(quiz: QuizTranslation): Single<Long>

    @Update
    fun update(quiz: QuizTranslation): Single<Long>

    @Delete
    fun delete(quiz: QuizTranslation): Single<Long>
}