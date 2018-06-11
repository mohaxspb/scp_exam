package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import ru.kuchanov.scpquiz.model.db.FinishedLevel


@Dao
interface FinishedLevelsDao {

    @Query("SELECT * FROM FinishedLevel")
    fun getAll(): Flowable<List<FinishedLevel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(finishedLevel: FinishedLevel): Long

    @Update
    fun update(finishedLevel: FinishedLevel): Int

    @Delete
    fun delete(finishedLevel: FinishedLevel): Int
}