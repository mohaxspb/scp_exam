package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType

@Dao
interface TransactionDao {

    @Query("SELECT * FROM QuizTransaction")
    fun getAll(): Flowable<List<QuizTransaction>>

    @Query("SELECT * FROM QuizTransaction")
    fun getAllSingle(): Single<List<QuizTransaction>>

    @Query("SELECT * FROM QuizTransaction")
    fun getAllList(): List<QuizTransaction>

    @Query("SELECT * FROM QuizTransaction WHERE id IN (:ids)")
    fun getAllByIds(ids: List<Long>): Single<List<QuizTransaction>>

    @Query("SELECT * FROM QuizTransaction WHERE id = :id")
    fun getOneById(id: Long): QuizTransaction

    @Query("SELECT * FROM QuizTransaction WHERE transactionType = :transactionType")
    fun getOneByType(transactionType: TransactionType): Single<QuizTransaction>

    @Insert
    fun insert(quizTransaction: QuizTransaction): Long

    @Insert
    fun insertQuizTransactionList(quizTransactionList: List<QuizTransaction>): List<Long>

    @Update
    fun update(quizTransaction: QuizTransaction): Int

    @Delete
    fun delete(quizTransaction: QuizTransaction): Int

    @Query("DELETE FROM QuizTransaction")
    fun deleteAll(): Int

    @Query("DELETE FROM QuizTransaction WHERE transactionType = 'NAME_WITH_PRICE' OR transactionType = 'NAME_NO_PRICE' OR transactionType = 'NAME_CHARS_REMOVED'" +
            " OR transactionType = 'NUMBER_WITH_PRICE' OR transactionType = 'NUMBER_NO_PRICE' OR transactionType = 'NUMBER_CHARS_REMOVED' OR transactionType = 'LEVEL_ENABLE_FOR_COINS' " +
            " OR transactionType = 'NAME_ENTERED_MIGRATION' OR transactionType = 'NUMBER_ENTERED_MIGRATION' OR transactionType = 'NAME_CHARS_REMOVED_MIGRATION' OR transactionType = 'NUMBER_CHARS_REMOVED_MIGRATION' OR transactionType = 'LEVEL_AVAILABLE_MIGRATION'")
    fun resetProgress(): Int

    @Query("UPDATE QuizTransaction SET coinsAmount = :coinsAmount WHERE transactionType = 'UPDATE_SYNC'")
    fun updateCoinsInSyncScoreTransaction(coinsAmount: Int?)

    @Query("UPDATE QuizTransaction SET externalId = :quizTransactionExternalId WHERE id = :quizTransactionId")
    fun updateQuizTransactionExternalId(quizTransactionId: Long, quizTransactionExternalId: Long)

    @Query("SELECT COUNT(*) FROM quiztransaction")
    fun getTransactionsCount(): Int
}