package ru.kuchanov.scpquiz.model.api

import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import javax.inject.Inject

class QuizConverter @Inject constructor() {

    fun convert(source: NwQuiz): Quiz {
        val quiz = Quiz(
            id = source.id,
            scpNumber = source.scpNumber,
            imageUrl = source.imageUrl,
            authorId = source.authorId,
            approved = source.approved,
            approverId = source.approverId,
            created = source.created,
            updated = source.updated
        )
        quiz.quizTranslations = convertCollection(source.quizTranslations, ::convert)
        return quiz;
    }

    fun convert(source: NwQuizTranslation) = QuizTranslation(
        id = source.id,
        quizId = 0,
        langCode = source.langCode,
        translation = source.translation,
        authorId = source.authorId,
        approved = source.approved,
        approverId = source.approverId,
        created = source.created,
        updated = source.updated
    )

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