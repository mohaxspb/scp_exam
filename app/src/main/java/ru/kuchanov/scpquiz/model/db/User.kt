package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class User(
    //db
    @PrimaryKey
    val id: Long,
    //content
    val name: String,
    val avatarUrl: String?,
    var score: Int
)