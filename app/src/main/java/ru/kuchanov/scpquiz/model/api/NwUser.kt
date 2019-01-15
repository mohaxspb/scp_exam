package ru.kuchanov.scpquiz.model.api

import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NwUser(
        @PrimaryKey
        val id: Long,
        //content
        val fullName: String?,
        val avatar: String?,
        val score: Long = 0
)