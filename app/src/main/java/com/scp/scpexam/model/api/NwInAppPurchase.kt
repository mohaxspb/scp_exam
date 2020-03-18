package com.scp.scpexam.model.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NwInAppPurchase(
        val id: Long,
        //content
        val transactionId: Long,
        val skuId: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val orderId: String
)