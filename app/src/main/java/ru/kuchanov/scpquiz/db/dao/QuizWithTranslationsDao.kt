package ru.kuchanov.scpquiz.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.QuizWithTranslations


@Dao
interface QuizWithTranslationsDao {

    @Query("SELECT * FROM quiz")
    fun getAll(): Flowable<List<QuizWithTranslations>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<QuizWithTranslations>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<QuizWithTranslations>
}