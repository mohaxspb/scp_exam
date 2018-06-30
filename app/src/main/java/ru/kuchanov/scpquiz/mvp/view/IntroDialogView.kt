package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.BaseView

interface IntroDialogView : BaseView {
    fun showChatMessage(message: String, user: User)
    fun showChatActions(chatActions: List<ChatAction>)
    fun removeChatAction(indexInParent: Int)
}