package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class InAppPurchase(
        @PrimaryKey
        val id: Long? = null,
        //content
        val transactionId: Long,
        val skuId: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val orderId: String
)