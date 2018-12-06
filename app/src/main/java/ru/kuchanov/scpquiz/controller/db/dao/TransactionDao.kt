package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import ru.kuchanov.scpquiz.model.db.Transaction

@Dao
interface TransactionDao {

    @Query("SELECT * FROM Transaction")
    fun getAll(): Flowable<List<Transaction>>

    @Insert
    fun insert(transaction: Transaction): Long

    @Update
    fun update(transaction: Transaction): Int

    @Delete
    fun delete(transaction: Transaction): Int
}