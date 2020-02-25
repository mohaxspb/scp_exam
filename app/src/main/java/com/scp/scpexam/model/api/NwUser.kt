package com.scp.scpexam.model.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NwUser(
        val id: Long,
        //content
        val fullName: String? = null,
        val avatar: String? = null,
        val score: Int = 0,
        val fullCompleteLevels: Int = 0,
        val partCompleteLevels: Int = 0
)