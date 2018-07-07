package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class QuizTranslationPhrase(
    //db
    @PrimaryKey
    val id: Long,
    var quizTranslationId: Long,
    //content
    val translation: String,
    //status
    val approved: Boolean,
    val authorId: Long,
    val approverId: Long?,
    //dates
    val created: Date,
    val updated: Date
) {
    override fun toString(): String {
        return "QuizTranslationPhrase( \n" +
                "id=$id, \n" +
                "quizTranslationId=$quizTranslationId, \n" +
                "translation='$translation', \n" +
                "approved=$approved, \n" +
                "authorId=$authorId, \n" +
                "approverId=$approverId, \n" +
                "created=$created, \n" +
                "updated=$updated \n" +
                ")"
    }
}