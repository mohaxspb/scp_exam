package com.scp.scpexam.model.util

import com.scp.scpexam.model.api.NwQuiz
import javax.inject.Inject

class QuizFilter @Inject constructor() {

    /**
     * remove all quizzes which not approved
     * remove all quizzes with no approved translations
     * remove all translation which not approved
     * remove all translations with no approved phrases
     * remove all phrases which not approved
     */
    fun filterQuizzes(quizzes: List<NwQuiz>): List<NwQuiz> {
        var filteredQuizzes = quizzes.toMutableList()

        //remove all phrases which not approved
        filteredQuizzes.forEach { quiz ->
            quiz.quizTranslations.map { translation ->
                translation.apply {
                    quizTranslationPhrases = quizTranslationPhrases
                            .filter { it.approved }
                            .toMutableList()
                }
            }
        }
        //remove all translations with no approved phrases
        filteredQuizzes.forEach { quiz ->
            quiz.quizTranslations = quiz.quizTranslations
                    .filter { it.quizTranslationPhrases.isNotEmpty() }
                    .toMutableList()
        }
        //remove all translation which not approved
        filteredQuizzes.forEach { quiz ->
            quiz.quizTranslations = quiz.quizTranslations
                    .filter { it.approved }
                    .toMutableList()
        }
        //remove all quizes with no approved translations
        filteredQuizzes = filteredQuizzes
                .filter { it.quizTranslations.isNotEmpty() }
                .toMutableList()

        //remove all quizes which not approved
        filteredQuizzes = filteredQuizzes
                .filter { it.approved }
                .toMutableList()

        return filteredQuizzes
    }
}