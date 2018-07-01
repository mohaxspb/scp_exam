package ru.kuchanov.scpquiz.model.ui

import android.support.annotation.DrawableRes

class ChatAction(
    val actionName: String,
    val action: (Int) -> Unit,
    @DrawableRes val bgResource: Int
) {
    override fun toString(): String {
        return "ChatAction(actionName='$actionName')"
    }
}