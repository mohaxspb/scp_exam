package com.scp.scpexam.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.scp.scpexam.R
import com.scp.scpexam.model.ui.ChatAction

@SuppressLint("ViewConstructor")
class ChatActionView @JvmOverloads constructor(
    chatAction: ChatAction,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_chat_action, this)

        setBackgroundResource(chatAction.bgResource)

        val chatActionTextView = findViewById<TextView>(R.id.chatActionTextView)
        chatActionTextView.text = chatAction.actionName

        val actionPriceTextView = findViewById<TextView>(R.id.actionPriceTextView)
        actionPriceTextView.text = chatAction.price.toString()
        actionPriceTextView.visibility = if (chatAction.price == 0) View.GONE else View.VISIBLE
    }
}