package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class Quiz(
    //db
    @PrimaryKey
    val id: Long,
    //content
    val scpNumber: String,
    val imageUrl: String,
    //status
    val authorId: Long,
    val approved: Boolean,
    val approverId: Long?,
    //dates
    val created: Date,
    val updated: Date
)