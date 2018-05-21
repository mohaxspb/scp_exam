package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation

data class QuizWithTranslations(
    @field:Embedded
    val quiz: Quiz,
    @field:Relation(parentColumn = "id", entityColumn = "quizId")
    val quizTranslation: List<QuizTranslation>
)