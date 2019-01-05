package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.http.*
import ru.kuchanov.scpquiz.model.api.NwQuizTransaction
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType

interface TransactionApi {

    @FormUrlEncoded
    @GET("transactions/{id}")
    fun getNwQuizTransactionById(
            @Header("Authorization") authorization: String,
            @Path("id") transactionId: Long
    ): Single<NwQuizTransaction>

    @FormUrlEncoded
    @GET("transactions/allByUserId")
    fun getNwQuizTransactionList(@Header("Authorization") authorization: String): Single<List<NwQuizTransaction>>

    @FormUrlEncoded
    @POST("transactions/add")
    fun addTransaction(
            @Header("Authorization") authorization: String,
            @Field("quizId") quizId: Long?,
            @Field("typeTransaction") typeTransaction: TransactionType,
            @Field("coinsAmount") coinsAmount: Long?
    ): Single<QuizTransaction>

    @FormUrlEncoded
    @POST("transactions/addAll")
    fun addAllTransactions(
            @Header("Authorization") authorization: String,
            @Field("transactions") transactions: List<QuizTransaction>
    ): Single<List<QuizTransaction>>
}