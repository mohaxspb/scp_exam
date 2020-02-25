package com.scp.scpexam.model.ui

import com.scp.scpexam.model.db.FinishedLevel
import com.scp.scpexam.model.db.Quiz
import com.scp.scpexam.model.db.QuizTranslation
import com.scp.scpexam.model.db.User

data class QuizLevelInfo(
    val quiz: Quiz,
    val randomTranslations: List<QuizTranslation>,
    val finishedLevel: FinishedLevel,
    val player: User,
    val doctor: User,
    val nextQuizIdAndFinishedLevel: Pair<Long?, FinishedLevel?>
)