package ru.kuchanov.scpquiz.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.Quiz


@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz")
    fun getAll(): Flowable<List<Quiz>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<Quiz>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<Quiz>

    @Insert
    fun insert(quiz: Quiz): Single<Long>

    @Update
    fun update(quiz: Quiz): Single<Long>

    @Delete
    fun delete(quiz: Quiz): Single<Long>
}