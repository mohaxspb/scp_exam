package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.FinishedLevel


@Dao
interface FinishedLevelsDao {

    @Query("SELECT * FROM FinishedLevel")
    fun getAll(): Flowable<List<FinishedLevel>>

    @Query("SELECT * FROM FinishedLevel")
    fun getAllList(): List<FinishedLevel>

    @Query("SELECT * FROM FinishedLevel")
    fun getAllSingle(): Single<List<FinishedLevel>>

    @Query("SELECT * FROM FinishedLevel ORDER BY quizId ASC")
    fun getAllByAsc(): Single<List<FinishedLevel>>

    @Query("SELECT COUNT(*) FROM FinishedLevel WHERE scpNameFilled = 1 OR scpNumberFilled = 1")
    fun getCountOfPartiallyFinishedLevels(): Long

    @Query("SELECT COUNT(*) FROM FinishedLevel WHERE scpNameFilled = 1 AND scpNumberFilled = 1")
    fun getCountOfFullyFinishedLevels(): Long

    @Query("SELECT * FROM FinishedLevel WHERE quizId = :quizId")
    fun getByIdWithUpdates(quizId: Long): Flowable<List<FinishedLevel>>

    @Query("SELECT * FROM FinishedLevel WHERE quizId = :quizId")
    fun getByIdOrErrorOnce(quizId: Long): Single<FinishedLevel>

    @Query("SELECT * FROM FinishedLevel WHERE quizId = :quizId")
    fun getByQuizId(quizId: Long): FinishedLevel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(finishedLevels: List<FinishedLevel>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(finishedLevel: FinishedLevel): Long

    @Update
    fun update(finishedLevel: FinishedLevel): Int

    @Update
    fun update(finishedLevels: List<FinishedLevel>): Int

    @Delete
    fun delete(finishedLevel: FinishedLevel): Int
}