package ru.kuchanov.scpquiz.controller.db.dao

import android.arch.persistence.room.*
import ru.kuchanov.scpquiz.model.db.InAppPurchase

@Dao
interface InAppPurchaseDao {

    @Query("SELECT COUNT(*) FROM InAppPurchase")
    fun getCount(): Long

    @Insert
    fun insert(inAppPurchase: InAppPurchase): Long

    @Update
    fun update(inAppPurchase: InAppPurchase): Int

    @Delete
    fun delete(inAppPurchase: InAppPurchase): Int
}