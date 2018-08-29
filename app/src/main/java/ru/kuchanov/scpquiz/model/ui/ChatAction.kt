package ru.kuchanov.scpquiz.model.ui

import android.support.annotation.DrawableRes

class ChatAction(
    val actionName: String,
    /**
     * Int is index of chat actions view in chat layout
     */
    val action: (Int) -> Unit,
    @DrawableRes val bgResource: Int,
    val price: Int = 0
) {
    override fun toString(): String {
        return "ChatAction(actionName='$actionName')"
    }
}

enum class ChatActionsGroupType {
    START_GAME, CHOOSE_ENTER_TYPE, LEVEL_FINISHED, NAME_ENTERED, NUMBER_ENTERED, SUGGESTIONS, GAIN_COINS
}