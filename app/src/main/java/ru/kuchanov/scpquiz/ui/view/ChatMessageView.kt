package ru.kuchanov.scpquiz.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import kotlinx.android.synthetic.main.view_chat_message_doctor.view.avatarImageView as doctorAvatar
import kotlinx.android.synthetic.main.view_chat_message_doctor.view.messageTextView as doctorMessage
import kotlinx.android.synthetic.main.view_chat_message_doctor.view.userNameTextView as doctorName
import kotlinx.android.synthetic.main.view_chat_message_player.view.avatarImageView as playerAvatar
import kotlinx.android.synthetic.main.view_chat_message_player.view.messageTextView as playerMessage
import kotlinx.android.synthetic.main.view_chat_message_player.view.userNameTextView as playerName

@SuppressLint("ViewConstructor")
class ChatMessageView @JvmOverloads constructor(
    val user: User? = null,
    val message: String = "",
    val nameTextColorRes: Int,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val mAvatarImageView: ImageView
    private val mMessageTextView: TextView
    private val mUserNameTextView: TextView

    init {
        if (user == null) {
            throw IllegalStateException("user must not be NULL!")
        }
        var avatarRes = 0
        when (user.role) {
            UserRole.DOCTOR -> {
                inflate(context, R.layout.view_chat_message_doctor, this)
                mAvatarImageView = doctorAvatar
                mMessageTextView = doctorMessage
                mUserNameTextView = doctorName
                avatarRes = R.drawable.ic_doctor
            }
            UserRole.PLAYER -> {
                inflate(context, R.layout.view_chat_message_player, this)
                mAvatarImageView = playerAvatar
                mMessageTextView = playerMessage
                mUserNameTextView = playerName
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

        mUserNameTextView.text = user.name
        mUserNameTextView.setTextColor(ContextCompat.getColor(context, nameTextColorRes))
    }
}