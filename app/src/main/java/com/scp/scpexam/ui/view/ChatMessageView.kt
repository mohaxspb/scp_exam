package com.scp.scpexam.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.RequestOptions
import com.scp.scpexam.R
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.db.UserRole
import com.scp.scpexam.ui.utils.GlideApp

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
                mAvatarImageView = findViewById<ImageView>(R.id.avatarImageView)
                mMessageTextView = findViewById<TextView>(R.id.messageTextView)
                mUserNameTextView = findViewById<TextView>(R.id.userNameTextView)
                avatarRes = R.drawable.ic_doctor
            }
            UserRole.PLAYER -> {
                inflate(context, R.layout.view_chat_message_player, this)
                mAvatarImageView = findViewById<ImageView>(R.id.avatarImageView)
                mMessageTextView = findViewById<TextView>(R.id.messageTextView)
                mUserNameTextView = findViewById<TextView>(R.id.userNameTextView)
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