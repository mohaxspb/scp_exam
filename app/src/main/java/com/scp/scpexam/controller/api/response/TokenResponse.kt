package com.scp.scpexam.controller.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(
        @Json(name = "access_token")
        val accessToken: String,
        @Json(name = "refresh_token")
        val refreshToken: String?,
        @Json(name = "token_type")
        val tokenType: String,
        @Json(name = "expires_in")
        val expiresIn: Int,
        @Json(name = "scope")
        val scope: String
)
