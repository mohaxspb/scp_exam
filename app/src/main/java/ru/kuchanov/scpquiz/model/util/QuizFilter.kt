package ru.kuchanov.scpquiz.model.util

import ru.kuchanov.scpquiz.model.api.NwQuiz

class QuizFilter {

    /**
     * remove all quizes which not approved
     * remove all quizes with no approved translations
     * remove all translation which not approved
     * remove all translations with no approved phrases
     * remove all phrases which not approved
     */
    fun filterQuizes(quizes: List<NwQuiz>): List<NwQuiz> {
        var filteredQuizes = quizes.toMutableList()

        //remove all phrases which not approved
        filteredQuizes.forEach { quiz ->
            quiz.quizTranslations.map { translation ->
                translation.apply {
                    quizTranslationPhrases = quizTranslationPhrases
                            .filter { it.approved }
                            .toMutableList()
                }
            }
        }
        //remove all translations with no approved phrases
        filteredQuizes.forEach { quiz ->
            quiz.quizTranslations = quiz.quizTranslations
                    .filter { it.quizTranslationPhrases.isNotEmpty() }
                    .toMutableList()
        }
        //remove all translation which not approved
        filteredQuizes.forEach { quiz ->
            quiz.quizTranslations = quiz.quizTranslations
                    .filter { it.approved }
                    .toMutableList()
        }
        //remove all quizes with no approved translations
        filteredQuizes = filteredQuizes.filter { it.quizTranslations.isNotEmpty() }.toMutableList()

        //remove all quizes which not approved
        filteredQuizes = filteredQuizes.filter { it.approved }.toMutableList()

        return filteredQuizes
    }
}