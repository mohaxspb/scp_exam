package ru.kuchanov.scpquiz.model.api

import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class NwQuizTranslationPhrase(
    //db
    val id: Long,
    //content
    val translation: String,
    //status
    val approved: Boolean,
    val authorId: Long?,
    val approverId: Long?,
    //dates
    val created: Date,
    val updated: Date
)