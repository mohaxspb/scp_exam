package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class User(
    //db
    @PrimaryKey
    val id: Long? = null,
    //content
    val name: String,
    val avatarUrl: String? = null,
    var score: Int = 0,
    val role: UserRole
)

enum class UserRole {
    PLAYER, DOCTOR, OTHER_USER
}
