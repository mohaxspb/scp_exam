package ru.kuchanov.scpquiz.model.api

import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.sql.Timestamp

@JsonClass(generateAdapter = true)
data class NwQuizTranslation(
    //db
    @PrimaryKey
    val id: Long,
    //content
    val langCode: String,
    val translation: String,
    //status
    val approved: Boolean,
    val authorId: Long,
    val approverId: Long?,
    //dates
    val created: Timestamp,
    val updated: Timestamp
)