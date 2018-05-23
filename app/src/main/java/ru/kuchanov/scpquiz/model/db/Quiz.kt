package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class Quiz(
    //db
    @PrimaryKey
    val id: Long,
    //content
    val scpNumber: String,
    val imageUrl: String,
    //status
    val authorId: Long,
    val approved: Boolean,
    val approverId: Long?,
    //dates
    val created: Date,
    val updated: Date
) {
    @Ignore
    var quizTranslations: List<QuizTranslation>? = null

    override fun toString(): String {
        return "Quiz(id=$id, scpNumber='$scpNumber', imageUrl='$imageUrl', authorId=$authorId, approved=$approved, approverId=$approverId, created=$created, updated=$updated, quizTranslations=$quizTranslations)"
    }
}