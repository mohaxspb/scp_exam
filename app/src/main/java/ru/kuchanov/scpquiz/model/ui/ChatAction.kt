package ru.kuchanov.scpquiz.model.ui

import android.support.annotation.DrawableRes

class ChatAction(
    val actionName: String,
    /**
     * Int is index of chat actions view in chat layout
     */
    val action: (Int) -> Unit,
    @DrawableRes val bgResource: Int
) {
    override fun toString(): String {
        return "ChatAction(actionName='$actionName')"
    }
}