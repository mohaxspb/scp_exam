package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.BaseView

interface GameView : BaseView {
    fun showProgress(show: Boolean)
    fun showError(error: Throwable)
    fun showChatMessage(message: String, user: User)
    fun showKeyboard(show: Boolean)
    fun removeChatAction(indexInParent: Int)
    fun showChatActions(chatActions: List<ChatAction>)
    fun setKeyboardChars(characters: List<Char>)
    fun showCoins(coins: Int)
    fun showToolbar(show: Boolean)
    fun setBackgroundDark(showDark: Boolean)
    fun animateKeyboard()
    fun showImage(quiz: Quiz)
    fun showLevelNumber(levelNumber: Int)
    fun showName(name: List<Char>)
    fun showNumber(number: List<Char>)
    fun onNeedToOpenSettings()
}