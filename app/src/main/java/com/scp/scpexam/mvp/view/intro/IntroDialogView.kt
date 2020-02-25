package com.scp.scpexam.mvp.view.intro

import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.ui.ChatAction
import com.scp.scpexam.model.ui.ChatActionsGroupType
import com.scp.scpexam.mvp.AuthView
import com.scp.scpexam.mvp.BaseView

interface IntroDialogView : BaseView, AuthView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showChatMessage(message: String, user: User)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showChatActions(chatActions: List<ChatAction>, chatActionsGroupType: ChatActionsGroupType)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun removeChatAction(indexInParent: Int)

}