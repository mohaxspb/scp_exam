package ru.kuchanov.scpquiz.mvp.view.game

import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface GameView : BaseView, AuthView {
    fun showProgress(show: Boolean)
    fun showError(error: Throwable)
    fun showChatMessage(message: String, user: User)
    fun showKeyboard(show: Boolean)
    fun removeChatAction(indexInParent: Int)
    fun showChatActions(chatActions: List<ChatAction>, chatActionsGroupType: ChatActionsGroupType)
    fun setKeyboardChars(characters: List<Char>)
    fun showCoins(coins: Int)
    fun showToolbar(show: Boolean)
    fun setBackgroundDark(showDark: Boolean)
    fun animateKeyboard()
    fun showImage(quiz: Quiz)
    fun showLevelNumber(levelNumber: Int)
    fun showName(name: List<Char>)
    fun showNumber(number: List<Char>)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenSettings()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenCoins()

    fun clearChatMessages()
    fun askForRateApp()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToShowRewardedVideo()

    fun addCharToNameInput(char: Char, charId: Int)
    fun addCharToNumberInput(char: Char, charId: Int)
    //    fun removeCharFromInput(charId: Int, indexOfChild: Int, isScpNameCompleted:Boolean)
    fun removeCharFromNameInput(charId: Int, indexOfChild: Int)

    fun removeCharFromNumberInput(charId: Int, indexOfChild: Int)
    fun showBackspaceButton(show: Boolean)
    fun showHelpButton(show: Boolean)
}