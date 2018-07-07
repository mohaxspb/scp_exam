package ru.kuchanov.scpquiz.model.api

import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class NwQuiz(
    //db
    @PrimaryKey
    val id: Long,
    //content
    val scpNumber: String,
    val imageUrl: String,
    val quizTranslations: MutableList<NwQuizTranslation>,
    //status
    val authorId: Long,
    val approved: Boolean,
    val approverId: Long?,
    //dates
    val created: Date,
    val updated: Date
)