package ru.kuchanov.scpquiz.model.api

import com.squareup.moshi.JsonClass
import ru.kuchanov.scpquiz.model.db.TransactionType

@JsonClass(generateAdapter = true)
data class NwQuizTransaction(
        val id: Long,
        //content
        var userId: Long? = null,
        var quizId: Long? = null,
        val quizTransactionType: TransactionType,
        val coinsAmount: Int? = null
)

