package ru.kuchanov.scpquiz.model.ui

data class ChatAction(
    val actionName: String,
    val action: () -> Unit
)