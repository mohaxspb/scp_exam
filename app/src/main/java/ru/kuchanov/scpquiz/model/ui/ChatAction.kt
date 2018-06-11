package ru.kuchanov.scpquiz.model.ui

class ChatAction(
    val actionName: String,
    val action: (Int) -> Unit
)