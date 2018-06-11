package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class FinishedLevel(
    @PrimaryKey
    val quizId: Long,
    var scpNameFilled: Boolean = false,
    var scpNumberFilled: Boolean = false
)