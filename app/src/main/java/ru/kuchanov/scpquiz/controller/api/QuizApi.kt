package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.http.*
import ru.kuchanov.scpquiz.controller.api.response.TokenResponse
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.NwUser

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
    fun getNwQuizList(@Header("Authorization") authorization: String): Single<List<NwQuiz>>

    @GET("user/score")
    fun getServerUserScore(): Single<Long>

    @GET("user/meClient")
    fun getNwUser(): Single<NwUser>
}

