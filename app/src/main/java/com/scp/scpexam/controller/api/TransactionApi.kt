package com.scp.scpexam.controller.api

import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import com.scp.scpexam.model.api.NwQuizTransaction
import com.scp.scpexam.model.api.NwUser
import com.scp.scpexam.model.db.TransactionType

interface TransactionApi {

    @GET("transactions/allByUserId")
    fun getNwQuizTransactionList(): Single<List<NwQuizTransaction>>

    @FormUrlEncoded
    @POST("transactions/add")
    fun addTransaction(
            @Field("quizId") quizId: Long?,
            @Field("typeTransaction") typeTransaction: TransactionType,
            @Field("coinsAmount") coinsAmount: Int?,
            @Field("createdOnClient") createdOnClient: String
    ): Single<NwQuizTransaction>

    @FormUrlEncoded
    @POST("inAppPurchase/add")
    fun addInAppPurchase(
            @Field("skuId") skuId: String,
            @Field("purchaseTime") purchaseTime: Long,
            @Field("purchaseToken") purchaseToken: String,
            @Field("orderId") orderId: String,
            @Field("coinsAmount") coinsAmount: Int
    ): Single<NwQuizTransaction>

    @GET("transactions/resetProgress")
    fun resetProgress(): Single<Int>

    @GET("user/meClient")
    fun getNwUser(): Single<NwUser>

    @GET("leaderboard/getUserForLeaderboard")
    fun getUserForLeaderboard(): Single<NwUser>

    @GET("leaderboard/currentPosition")
    fun getCurrentPositionInLeaderboard(): Single<Int>

}