package com.scp.scpexam.controller.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import com.scp.scpexam.controller.api.response.PurchaseValidateResponse

interface ToolsApi {

    @GET("purchaseValidation/validate")
    fun validatePurchase(
        @Query("isSubscription") isSubscription: Boolean,
        @Query("package") packageName: String,
        @Query("sku") sku: String,
        @Query("purchaseToken") purchaseToken: String
    ): Single<PurchaseValidateResponse>
}