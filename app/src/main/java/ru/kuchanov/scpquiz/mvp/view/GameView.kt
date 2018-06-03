package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.mvp.BaseView

interface GameView : BaseView {
    fun showProgress(show: Boolean)
    fun showLevel(quiz: Quiz, randomTranslations: List<QuizTranslation>)
    fun showError(error: Throwable)
}