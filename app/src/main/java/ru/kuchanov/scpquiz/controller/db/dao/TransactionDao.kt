package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.QuizTransaction

@Dao
interface TransactionDao {

    @Query("SELECT * FROM QuizTransaction")
    fun getAll(): Flowable<List<QuizTransaction>>

    @Query("SELECT * FROM QuizTransaction WHERE id IN (:ids)")
    fun getAllByIds(ids: List<Long>): Single<List<QuizTransaction>>

    @Query("SELECT * FROM QuizTransaction WHERE id = :id")
    fun getOneById(id: Long): QuizTransaction

//    @Query("SELECT * FROM QuizTransaction WHERE id = :id")
//    fun getQuizIdById(id: Long): Long?

    @Insert
    fun insert(quizTransaction: QuizTransaction): Long

    @Insert
    fun insertQuizTransactionList(quizTransactionList: List<QuizTransaction>): List<Long>

    @Update
    fun update(quizTransaction: QuizTransaction): Int

    @Delete
    fun delete(quizTransaction: QuizTransaction): Int

    @Query("UPDATE QuizTransaction SET externalId = :quizTransactionExternalId WHERE id = :quizTransactionId")
    fun updateQuizTransactionExternalId(quizTransactionId: Long, quizTransactionExternalId: Long)

    @Query("SELECT COUNT(*) FROM quiztransaction")
    fun getTransactionsCount(): Int
}