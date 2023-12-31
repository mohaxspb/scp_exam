package com.scp.scpexam.controller.db.dao

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.db.UserRole


@Dao
interface UserDao {

    @Query("SELECT * FROM User")
    fun getAll(): Flowable<List<User>>

    @Query("SELECT * FROM User WHERE id = :id")
    fun getByIdWithUpdates(id: Long): Flowable<List<User>>

    @Query("SELECT * FROM User WHERE id = :id")
    fun getByIdOrErrorOnce(id: Long): Single<User>

    @Query("SELECT * FROM User WHERE role = :role limit 1")
    fun getOneByRole(role: UserRole): Single<User>

    @Query("SELECT * FROM User WHERE role = :role limit 1")
    fun getOneByRoleSync(role: UserRole): User?

    @Query("SELECT * FROM User WHERE role = :role limit 1")
    fun getByRoleWithUpdates(role: UserRole): Flowable<List<User>>

    @Insert
    fun insert(user: User): Long

    @Update
    fun update(user: User): Int

    @Delete
    fun delete(user: User): Int
}