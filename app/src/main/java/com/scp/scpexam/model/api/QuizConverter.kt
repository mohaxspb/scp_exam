package com.scp.scpexam.model.api

import com.scp.scpexam.model.db.Quiz
import com.scp.scpexam.model.db.QuizTransaction
import com.scp.scpexam.model.db.QuizTranslation
import com.scp.scpexam.model.db.QuizTranslationPhrase
import javax.inject.Inject

class QuizConverter @Inject constructor() {

    fun convert(source: NwQuiz): Quiz {
        val result = Quiz(
                id = source.id,
                scpNumber = source.scpNumber,
                imageUrl = source.imageUrl,
                authorId = source.authorId,
                approved = source.approved,
                approverId = source.approverId,
                created = source.created,
                updated = source.updated
        )
        result.quizTranslations = convertCollection(source.quizTranslations, ::convert)
        return result
    }

    private fun convert(source: NwQuizTranslation): QuizTranslation {
        val result = QuizTranslation(
                id = source.id,
                quizId = 0,
                langCode = source.langCode,
                translation = source.translation,
                description = source.description,
                authorId = source.authorId,
                approved = source.approved,
                approverId = source.approverId,
                created = source.created,
                updated = source.updated
        )

        result.quizTranslationPhrases = convertCollection(source.quizTranslationPhrases, ::convert)
        return result
    }

    private fun convert(source: NwQuizTranslationPhrase) = QuizTranslationPhrase(
            id = source.id,
            quizTranslationId = 0,
            translation = source.translation,
            authorId = source.authorId,
            approved = source.approved,
            approverId = source.approverId,
            created = source.created,
            updated = source.updated
    )

    fun convert(source: NwQuizTransaction): QuizTransaction {
        return QuizTransaction(
                quizId = source.quizId,
                externalId = source.id,
                transactionType = source.quizTransactionType,
                coinsAmount = source.coinsAmount,
                createdOnClient = source.createdOnClient
        )
    }

//    fun <T, W> convertCollection(collection: Iterable<T>, convertFunction: Function1<T, W>): List<W> {
//        val result = mutableListOf<W>()
//        for (source in collection) {
//            try {
//                result.add(convertFunction.invoke(source))
//            } catch (e: NoSuchElementException) {
//                Timber.e(e.message)
//            } catch (e: Exception) {
//                Timber.e(e)
//            }
//        }
//        return result
//    }

    fun <T, W> convertCollection(collection: List<T>, convertFunction: Function1<T, W>) = collection.map(convertFunction)
}