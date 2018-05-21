package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class QuizTranslation(
    //db
    @PrimaryKey
    val id: Long,
    var quizId: Long,
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