package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.*
import ru.kuchanov.scpquiz.model.api.NwQuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType

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

    @GET("transactions/resetProgress")
    fun resetProgress(): Single<Int>
}