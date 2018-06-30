package ru.kuchanov.scpquiz.ui.utils

import android.animation.ObjectAnimator
import android.graphics.Color
import android.support.v4.widget.NestedScrollView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.ui.view.ChatMessageView
import ru.kuchanov.scpquiz.utils.SystemUtils
import timber.log.Timber

class ChatDelegate(
    private val chatView: LinearLayout,
    private val scrollView: NestedScrollView,
    private val myPreferenceManager: MyPreferenceManager
) {

    fun showChatMessage(message: String, user: User) {
        val chatMessageView = ChatMessageView(
            context = chatView.context,
            user = user,
            message = message
        )

        chatView.addView(chatMessageView)

        chatMessageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = chatMessageView.width
                val height = chatMessageView.height
                if (width > 0 && height > 0) {
                    chatMessageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    ObjectAnimator
                            .ofInt(scrollView, "scrollY", chatMessageView.top)
                            .setDuration(500)
                            .start()

                    if (myPreferenceManager.isVibrationEnabled()) {
                        SystemUtils.vibrate()
                    }
                }
            }
        })
    }

    fun showChatActions(chatActions: List<ChatAction>) {
        Timber.d("showChatActions: ${chatActions.joinToString()}")
        val chatActionsFlexBoxLayout = LayoutInflater
                .from(chatView.context)
                .inflate(R.layout.view_chat_actions, chatView, false) as FlexboxLayout
        chatView.addView(chatActionsFlexBoxLayout)

        chatActions.forEach { chatAction ->
            //todo correct design
            val chatActionView = TextView(chatActionsFlexBoxLayout.context)
            chatActionsFlexBoxLayout.addView(chatActionView)
            chatActionView.text = chatAction.actionName
            chatActionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            chatActionView.setBackgroundColor(Color.YELLOW)
            chatActionView.setPadding(20, 20, 20, 20)
            chatActionView.setOnClickListener {
                if (myPreferenceManager.isVibrationEnabled()) {
                    SystemUtils.vibrate()
                }
                chatAction.action.invoke(chatView.indexOfChild(chatActionsFlexBoxLayout))
            }

            chatActionView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val width = chatActionView.width
                    val height = chatActionView.height
                    if (width > 0 && height > 0) {
                        chatActionView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        ObjectAnimator
                                .ofInt(scrollView, "scrollY", chatActionView.top)
                                .setDuration(500)
                                .start()

                        if (myPreferenceManager.isVibrationEnabled()) {
                            SystemUtils.vibrate()
                        }
                    }
                }
            })
        }
    }

    fun removeChatAction(indexInParent: Int) {
        chatView.removeViewAt(indexInParent)
    }
}