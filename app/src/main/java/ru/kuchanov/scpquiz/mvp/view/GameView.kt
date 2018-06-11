package ru.kuchanov.scpquiz.mvp.view

import android.view.View
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.BaseView

interface GameView : BaseView {
    fun showProgress(show: Boolean)
    fun showLevel(quiz: Quiz, randomTranslations: List<QuizTranslation>)
    fun showError(error: Throwable)
    fun showLevelCompleted()
    //    fun showScpNameEntered()
    fun showChatMessage(message: String, user: User)

    fun showKeyboard(show: Boolean)
//    fun showChatActions(chatActions: List<ChatAction>)
    fun removeChatAction(indexInParent:Int)
    fun showChatActions(chatActions: List<ChatAction>, containerId: Int)
    fun setKeyboardChars(characters: List<Char>)
}