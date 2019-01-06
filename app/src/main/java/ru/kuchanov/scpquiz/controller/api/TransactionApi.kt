package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.http.*
import ru.kuchanov.scpquiz.model.api.NwQuizTransaction
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType

interface TransactionApi {

    @GET("transactions/{id}")
    fun getNwQuizTransactionById(
            @Path("id") transactionId: Long
    ): Single<NwQuizTransaction>

    @GET("transactions/allByUserId")
    fun getNwQuizTransactionList(): Single<List<NwQuizTransaction>>

    @FormUrlEncoded
    @POST("transactions/add")
    fun addTransaction(
            @Field("quizId") quizId: Long?,
            @Field("typeTransaction") typeTransaction: TransactionType,
            @Field("coinsAmount") coinsAmount: Int?
    ): Single<NwQuizTransaction>

    @FormUrlEncoded
    @POST("transactions/addAll")
    fun addAllTransactions(
            @Field("transactions") transactions: List<QuizTransaction>
    ): Single<List<NwQuizTransaction>>
}