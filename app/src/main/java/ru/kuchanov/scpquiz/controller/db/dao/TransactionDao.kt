package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import ru.kuchanov.scpquiz.model.db.QuizTransaction

@Dao
interface TransactionDao {

    @Query("SELECT * FROM QuizTransaction")
    fun getAll(): Flowable<List<QuizTransaction>>

    @Insert
    fun insert(quizTransaction: QuizTransaction): Long

    @Update
    fun update(quizTransaction: QuizTransaction): Int

    @Delete
    fun delete(quizTransaction: QuizTransaction): Int
}