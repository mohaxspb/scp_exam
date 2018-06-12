package ru.kuchanov.scpquiz.model.ui

class ChatAction(
    val actionName: String,
    val action: (Int) -> Unit
) {
    override fun toString(): String {
        return "ChatAction(actionName='$actionName')"
    }
}