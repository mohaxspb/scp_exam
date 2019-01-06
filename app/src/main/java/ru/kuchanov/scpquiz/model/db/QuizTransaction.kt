package ru.kuchanov.scpquiz.model.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class QuizTransaction(
        //db
        @PrimaryKey
        val id: Long? = null,
        //content
        var quizId: Long? = null,
        val externalId: Long? = null,
        val transactionType: TransactionType,
        val coinsAmount: Int? = null
)

enum class TransactionType {
    NAME_WITH_PRICE,
    NAME_NO_PRICE,
    NAME_CHARS_REMOVED,
    NUMBER_WITH_PRICE,
    NUMBER_NO_PRICE,
    NUMBER_CHARS_REMOVED,
    LEVEL_ENABLE_FOR_COINS,
    ADV_WATCHED,
    ADV_BUY_NEVER_SHOW
}


