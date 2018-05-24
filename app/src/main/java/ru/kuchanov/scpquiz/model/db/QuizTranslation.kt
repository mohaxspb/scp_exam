package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

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
    val created: Date,
    val updated: Date
) {
    @Ignore
    var quizTranslationPhrases: List<QuizTranslationPhrase>? = null

    override fun toString(): String {
        return "QuizTranslation(\n" +
                "id=$id, \n" +
                "quizId=$quizId, \n" +
                "langCode='$langCode', \n" +
                "translation='$translation', \n" +
                "approved=$approved, \n" +
                "authorId=$authorId, \n" +
                "approverId=$approverId, \n" +
                "created=$created, \n" +
                "updated=$updated, \n" +
                "quizTranslationPhrases=$quizTranslationPhrases\n" +
                ")\n\n"
    }
}