package com.scp.scpexam.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FinishedLevel(
    @PrimaryKey
    val quizId: Long,
    var scpNameFilled: Boolean = false,
    var scpNumberFilled: Boolean = false,
    var nameRedundantCharsRemoved: Boolean = false,
    var numberRedundantCharsRemoved: Boolean = false,
    var isLevelAvailable: Boolean = false
)