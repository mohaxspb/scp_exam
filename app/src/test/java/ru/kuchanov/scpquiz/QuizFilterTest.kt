package ru.kuchanov.scpquiz

import android.support.test.filters.SmallTest
import org.junit.Assert.assertTrue
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
    fun quizFilter_emptyList() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.isEmpty())
    }

    @Test
    fun quizFilter_allApproved() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val phrase0 = createPhrase(true)
        val translation0 = createTranslation(true, listOf(phrase0))
        val quiz0 = createQuiz(true, listOf(translation0))
        quizesToTest += quiz0

        val phrase1 = createPhrase(true)
        val translation1 = createTranslation(true, listOf(phrase1))
        val quiz1 = createQuiz(true, listOf(translation1))
        quizesToTest += quiz1

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.size == 2)
        filteredQuizes.forEach { quiz ->
            assertTrue(quiz.quizTranslations.size == 1)
            quiz.quizTranslations.forEach {
                assertTrue(it.quizTranslationPhrases.size == 1)
            }
        }
    }

    @Test
    fun quizFilter_allApprovedWithoutTranslations() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val quiz0 = createQuiz(true, listOf())
        quizesToTest.add(quiz0)

        val quiz1 = createQuiz(true, listOf())
        quizesToTest.add(quiz1)

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.isEmpty())
    }

    @Test
    fun quizFilter_allApprovedWithoutTranslationPhrases() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val translation0 = createTranslation(true, listOf())
        val quiz0 = createQuiz(true, listOf(translation0))
        quizesToTest.add(quiz0)

        val translation1 = createTranslation(true, listOf())
        val quiz1 = createQuiz(true, listOf(translation1))
        quizesToTest.add(quiz1)

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.isEmpty())
    }

    @Test
    fun quizFilter_allApprovedExceptOfTranslation() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val phrase0 = createPhrase(true)
        val translation0 = createTranslation(false, listOf(phrase0))
        val quiz0 = createQuiz(true, listOf(translation0))
        quizesToTest.add(quiz0)

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.isEmpty())
    }

    @Test
    fun quizFilter_allApprovedExceptOfSomeTranslations() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val phrase0 = createPhrase(true)
        val translation0 = createTranslation(true, listOf(phrase0))

        val phrase1 = createPhrase(true)
        val translation1 = createTranslation(false, listOf(phrase1))

        val quiz0 = createQuiz(true, listOf(translation0, translation1))
        quizesToTest.add(quiz0)

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.size == 1)
    }

    @Test
    fun quizFilter_allApprovedExceptOfPhrase() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val phrase0 = createPhrase(false)
        val translation0 = createTranslation(true, listOf(phrase0))
        val quiz0 = createQuiz(true, listOf(translation0))
        quizesToTest.add(quiz0)

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.isEmpty())
    }

    @Test
    fun quizFilter_allApprovedExceptOfSomePhrases() {
        val quizesToTest = mutableListOf<NwQuiz>()

        val phrase0 = createPhrase(false)
        val phrase1 = createPhrase(true)
        val translation0 = createTranslation(true, listOf(phrase0, phrase1))
        val quiz0 = createQuiz(true, listOf(translation0))
        quizesToTest.add(quiz0)

        val filteredQuizes = quizFilter.filterQuizzes(quizesToTest)

        assertTrue(filteredQuizes.size == 1)
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