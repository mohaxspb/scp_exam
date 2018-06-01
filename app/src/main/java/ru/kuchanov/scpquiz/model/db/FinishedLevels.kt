package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class FinishedLevels(
    //db
    @PrimaryKey
    val id: Long? = null,
    //content
    val quizId: Long,
    var finished: Boolean
)