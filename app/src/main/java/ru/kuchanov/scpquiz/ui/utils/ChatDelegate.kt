package ru.kuchanov.scpquiz.ui.utils

import android.animation.ObjectAnimator
import android.support.annotation.ColorRes
import android.support.v4.widget.NestedScrollView
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
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

    fun showChatMessage(message: String, user: User, @ColorRes nameTextColorRes: Int) {
        Timber.d("showChatMessage from ${user.name}")
        val chatMessageView = ChatMessageView(
            context = chatView.context,
            user = user,
            message = message,
            nameTextColorRes = nameTextColorRes
        )

        chatView.addView(chatMessageView)

        chatMessageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = chatMessageView.width
                val height = chatMessageView.height
                if (width > 0 && height > 0) {
                    chatMessageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    ObjectAnimator
                            .ofInt(scrollView, "scrollY", chatMessageView.bottom)
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
        val inflater = LayoutInflater.from(chatView.context)

        val chatActionsFlexBoxLayout = inflater.inflate(
            R.layout.view_chat_actions,
            chatView,
            false
        ) as FlexboxLayout
        chatView.addView(chatActionsFlexBoxLayout)

        chatActions.forEach { chatAction ->
            val chatActionView = inflater.inflate(
                R.layout.view_chat_action,
                chatView,
                false
            ) as TextView
            chatActionsFlexBoxLayout.addView(chatActionView)
            chatActionView.text = chatAction.actionName
            chatActionView.setBackgroundResource(chatAction.bgResource)
            chatActionView.setOnClickListener {
                if (myPreferenceManager.isVibrationEnabled()) {
                    SystemUtils.vibrate()
                }
                chatAction.action.invoke(chatView.indexOfChild(chatActionsFlexBoxLayout))
            }
        }

        chatActionsFlexBoxLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = chatActionsFlexBoxLayout.width
                val height = chatActionsFlexBoxLayout.height
                if (width > 0 && height > 0) {
                    Timber.d("onGlobalLayout with not 0 sizes, so start scroll animation")
                    chatActionsFlexBoxLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    ObjectAnimator
                            .ofInt(scrollView, "scrollY", chatActionsFlexBoxLayout.bottom)
                            .setDuration(500)
                            .start()

                    if (myPreferenceManager.isVibrationEnabled()) {
                        SystemUtils.vibrate()
                    }
                }
            }
        })
    }

    fun removeChatAction(indexInParent: Int) {
        chatView.removeViewAt(indexInParent)
    }
}