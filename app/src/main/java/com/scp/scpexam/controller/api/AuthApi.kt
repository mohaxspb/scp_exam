package com.scp.scpexam.controller.api

import io.reactivex.Single
import retrofit2.http.*
import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.response.TokenResponse
import com.scp.scpexam.model.api.NwUser

interface AuthApi {

    @FormUrlEncoded
    @POST("oauth/token")
    fun getAccessToken(
            @Header("Authorization") testAuthorization: String,
            @Field("grant_type") testGrantType: String,
            @Field("username") username: String? = null,
            @Field("password") password: String? = null
    ): Single<TokenResponse>

    @FormUrlEncoded
    @POST("oauth/token")
    fun getAccessTokenByRefreshToken(
            @Header("Authorization") testAuthorization: String,
            @Field("grant_type") testRefreshToken: String,
            @Field("refresh_token") testRefreshTokenValue: String
    ): Single<TokenResponse>

    @FormUrlEncoded
    @POST("auth/socialLogin")
    fun socialLogin(
            @Field("provider") provider: Constants.Social,
            @Field("token") tokenValue: String,
            @Field("clientId") clientId: String,
            @Field("clientSecret") clientSecret: String,
            @Field("clientApp") clientApp: String
    ): Single<TokenResponse>

    @GET("leaderboard/getLeaderboard")
    fun getLeaderboard(
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
    ): Single<List<NwUser>>
}