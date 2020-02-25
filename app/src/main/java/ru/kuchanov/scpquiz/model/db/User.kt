package ru.kuchanov.scpquiz.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.Context
import ru.kuchanov.scpquiz.R
import java.util.*

@Entity
data class User(
        //db
        @PrimaryKey
        val id: Long? = null,
        //content
        var name: String,
        var avatarUrl: String? = null,
        var score: Int = 0,
        val role: UserRole
)

enum class UserRole {
    PLAYER, DOCTOR, OTHER_USER
}

fun generateRandomName(context: Context) = context.getString(R.string.player_name, Random().nextInt(10000))