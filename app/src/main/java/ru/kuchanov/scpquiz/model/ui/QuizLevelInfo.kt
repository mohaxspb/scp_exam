package ru.kuchanov.scpquiz.model.ui

import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.model.db.User

data class QuizLevelInfo(
    val quiz: Quiz,
    val randomTranslations: List<QuizTranslation>,
    val finishedLevel: FinishedLevel? = null,
    val player: User,
    val doctor: User

)