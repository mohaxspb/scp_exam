package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import ru.kuchanov.scpquiz.controller.api.response.TokenResponse
import ru.kuchanov.scpquiz.model.api.NwQuiz

interface QuizApi {

    @FormUrlEncoded
    @POST("oauth/token")
    fun getAccessToken(
            @Header("Authorization") authorization: String,
            @Field("grant_type") grantType: String
    ): Single<TokenResponse>


    @GET("quiz/all")
    fun getNwQuizList(@Header("Authorization") authorization: String): Single<List<NwQuiz>>
}

