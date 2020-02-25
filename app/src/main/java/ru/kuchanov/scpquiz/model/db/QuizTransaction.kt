package ru.kuchanov.scpquiz.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class QuizTransaction(
        //db
        @PrimaryKey
        val id: Long? = null,
        //content
        var quizId: Long? = null,
        val externalId: Long? = null,
        val transactionType: TransactionType,
        val coinsAmount: Int? = null,
        //dates
        val createdOnClient: Long = System.currentTimeMillis()
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
    ADV_BUY_NEVER_SHOW,
    UPDATE_SYNC,
    // енум для синхронизации всех очков юзера при скачивании новой версии
    NAME_ENTERED_MIGRATION,
    NUMBER_ENTERED_MIGRATION,
    NAME_CHARS_REMOVED_MIGRATION,
    NUMBER_CHARS_REMOVED_MIGRATION,
    LEVEL_AVAILABLE_MIGRATION,
    // енумы только для синхронизации с новой версией
    INAPP_PURCHASE
    // енум для покупки за деньги
}




