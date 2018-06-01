package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.FinishedLevels


@Dao
interface FinishedLevelsDao {

    @Query("SELECT * FROM FinishedLevels")
    fun getAll(): Flowable<List<FinishedLevels>>

    @Query("SELECT * FROM FinishedLevels WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<FinishedLevels>>

    @Query("SELECT * FROM FinishedLevels WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<FinishedLevels>

    @Insert
    fun insert(finishedLevels: FinishedLevels): Long

    @Update
    fun update(finishedLevels: FinishedLevels): Int

    @Delete
    fun delete(finishedLevels: FinishedLevels): Int
}