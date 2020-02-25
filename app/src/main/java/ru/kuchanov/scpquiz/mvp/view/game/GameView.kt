package ru.kuchanov.scpquiz.mvp.view.game

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface GameView : BaseView, AuthView {
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showError(error: Throwable)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showChatMessage(message: String, user: User)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showKeyboard(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun removeChatAction(indexInParent: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showChatActions(chatActions: List<ChatAction>, chatActionsGroupType: ChatActionsGroupType)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun setKeyboardChars(characters: List<Char>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showCoins(coins: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToolbar(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun setBackgroundDark(showDark: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun animateKeyboard()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showImage(quiz: Quiz)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showLevelNumber(levelNumber: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showName(name: List<Char>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showNumber(number: List<Char>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenSettings()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToOpenCoins()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun clearChatMessages()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun askForRateApp()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToShowRewardedVideo()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNeedToBuyCoins(skuId: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun addCharToNameInput(char: Char, charId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun addCharToNumberInput(char: Char, charId: Int)
    //    fun removeCharFromInput(charId: Int, indexOfChild: Int, isScpNameCompleted:Boolean)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun removeCharFromNameInput(charId: Int, indexOfChild: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun removeCharFromNumberInput(charId: Int, indexOfChild: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showBackspaceButton(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showHelpButton(show: Boolean)

}