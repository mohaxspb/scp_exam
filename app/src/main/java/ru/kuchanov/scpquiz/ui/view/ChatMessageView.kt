package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import kotlinx.android.synthetic.main.view_chat_message.view.avatarImageView as doctorAvatar
import kotlinx.android.synthetic.main.view_chat_message.view.messageTextView as doctorMessage
import kotlinx.android.synthetic.main.view_chat_message_player.view.avatarImageView as playerAvatar
import kotlinx.android.synthetic.main.view_chat_message_player.view.messageTextView as playerMessage

class ChatMessageView @JvmOverloads constructor(
    val user: User? = null,
    val message: String = "",
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val mAvatarImageView: ImageView
    private val mMessageTextView: TextView

    init {
        if (user == null) {
            throw IllegalStateException("user must not be NULL!")
        }
        var avatarRes = 0
        when (user.role) {
            UserRole.DOCTOR -> {
                inflate(context, R.layout.view_chat_message, this)
                mAvatarImageView = doctorAvatar
                mMessageTextView = doctorMessage
                avatarRes = R.drawable.ic_doctor
            }
            UserRole.PLAYER -> {
                inflate(context, R.layout.view_chat_message_player, this)
                mAvatarImageView = playerAvatar
                mMessageTextView = playerMessage
                avatarRes = R.drawable.ic_player
            }
            UserRole.OTHER_USER -> throw NotImplementedError("implement user role for ChatMessageView")
        }


        val glideRequest = GlideApp.with(context)
        when (user.avatarUrl) {
            null -> glideRequest.load(avatarRes)
            else -> glideRequest.load(user.avatarUrl)
        }
                .apply(RequestOptions.circleCropTransform())
                .into(mAvatarImageView)

        mMessageTextView.text = message
    }
}