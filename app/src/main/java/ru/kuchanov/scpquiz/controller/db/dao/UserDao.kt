package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kuchanov.scpquiz.model.db.User


@Dao
interface UserDao {

    @Query("SELECT * FROM User")
    fun getAll(): Flowable<List<User>>

    @Query("SELECT * FROM User WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<User>>

    @Query("SELECT * FROM User WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<User>

    @Insert
    fun insert(user: User): Long

    @Update
    fun update(user: User): Int

    @Delete
    fun delete(user: User): Int
}