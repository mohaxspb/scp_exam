package ru.kuchanov.scpquiz

import android.support.test.filters.SmallTest
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.NwQuizTranslation
import ru.kuchanov.scpquiz.model.api.NwQuizTranslationPhrase
import ru.kuchanov.scpquiz.model.util.QuizFilter
import java.util.*

@SmallTest
class QuizFilterTest {

    private lateinit var quizFilter: QuizFilter

    @Before
    fun initQuizFilter() {
        quizFilter = QuizFilter()
    }

    @Test
    fun quizFilter_allApproved_ReturnsTrue() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val phrase0 = createPhrase(true)
        val translation0 = createTranslation(true, listOf(phrase0))
        val quiz0 = createQuiz(true, listOf(translation0))
        quizesToTest.add(quiz0)

        val filteredQuizes = quizFilter.filterQuizes(quizesToTest)

        assertTrue(filteredQuizes.size == 1)
        filteredQuizes.forEach {  }
    }

    private fun createQuiz(
            approved: Boolean,
            translations: List<NwQuizTranslation>
    ) =
            NwQuiz(
                    id = Random().nextLong(),
                    approved = approved,
                    authorId = Random().nextLong(),
                    approverId = Random().nextLong(),
                    created = Date(),
                    updated = Date(),
                    imageUrl = "test",
                    scpNumber = "1",
                    quizTranslations = translations.toMutableList()
            )

    private fun createTranslation(
            approved: Boolean,
            phrases: List<NwQuizTranslationPhrase>
    ) =
            NwQuizTranslation(
                    id = Random().nextLong(),
                    langCode = "ru",
                    translation = "test",
                    description = "test",
                    approved = approved,
                    authorId = Random().nextLong(),
                    approverId = Random().nextLong(),
                    created = Date(),
                    updated = Date(),
                    quizTranslationPhrases = phrases.toMutableList()
            )

    private fun createPhrase(approved: Boolean) =
            NwQuizTranslationPhrase(
                    id = Random().nextLong(),
                    translation = "test",
                    approved = approved,
                    authorId = Random().nextLong(),
                    approverId = Random().nextLong(),
                    created = Date(),
                    updated = Date()
            )
}