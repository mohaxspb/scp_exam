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
        return "Quiz(\n" +
                "id=$id, \n" +
                "scpNumber='$scpNumber', \n" +
                "imageUrl='$imageUrl', \n" +
                "authorId=$authorId, \n" +
                "approved=$approved, \n" +
                "approverId=$approverId, \n" +
                "created=$created, \n" +
                "updated=$updated, \n" +
                "quizTranslations=$quizTranslations\n" +
                ")\n\n"
    }
}