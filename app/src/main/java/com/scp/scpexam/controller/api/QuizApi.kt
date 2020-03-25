package com.scp.scpexam.controller.api

import io.reactivex.Single
import retrofit2.http.*
import com.scp.scpexam.controller.api.response.TokenResponse
import com.scp.scpexam.model.api.NwQuiz
import io.reactivex.Flowable

interface QuizApi {

    /**
     * use it only for get quizList if user doesn't log in.
     */
    @FormUrlEncoded
    @POST("oauth/token")
    fun getUnloginedUserAccessToken(
            @Header("Authorization") authorization: String,
            @Field("grant_type") grantType: String
    ): Single<TokenResponse>

    /**
     * use token from [getUnloginedUserAccessToken]
     */
    @GET("quiz/all")
    fun getNwQuizList(): Single<List<NwQuiz>>

    @GET("quiz/all/pagination")
    fun getNwQuizListPaging(
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
    ): Flowable<List<NwQuiz>>

}

