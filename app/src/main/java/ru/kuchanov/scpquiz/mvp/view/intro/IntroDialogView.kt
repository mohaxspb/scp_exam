package ru.kuchanov.scpquiz.mvp.view.intro

import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface IntroDialogView : BaseView, AuthView {
    fun showChatMessage(message: String, user: User)
    fun showChatActions(chatActions: List<ChatAction>, chatActionsGroupType: ChatActionsGroupType)
    fun removeChatAction(indexInParent: Int)

}