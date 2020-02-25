package ru.kuchanov.scpquiz.mvp.view.intro

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface IntroDialogView : BaseView, AuthView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showChatMessage(message: String, user: User)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showChatActions(chatActions: List<ChatAction>, chatActionsGroupType: ChatActionsGroupType)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun removeChatAction(indexInParent: Int)

}